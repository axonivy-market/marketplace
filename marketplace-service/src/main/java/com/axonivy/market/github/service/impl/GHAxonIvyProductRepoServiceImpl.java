package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
	private GHOrganization organization;
	private final GitHubService gitHubService;
	private String repoUrl;
	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static final String DEMO_SETUP_TITLE = "(?i)## Demo|## Setup";
	public static final String IMAGE_EXTENSION = "(.*?).(jpeg|jpg|png)";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService) {
		this.gitHubService = gitHubService;
	}

	@Override
	public List<MavenArtifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
		List<MavenArtifact> artifacts = new ArrayList<>();
		InputStream contentStream = extractedContentStream(content);
		if (Objects.isNull(contentStream)) {
			return artifacts;
		}

		JsonNode rootNode = objectMapper.readTree(contentStream);
		JsonNode installersNode = rootNode.path(ProductJsonConstants.INSTALLERS);

		for (JsonNode mavenNode : installersNode) {
			JsonNode dataNode = mavenNode.path(ProductJsonConstants.DATA);

			// Not convert to artifact if id of node is not maven-import or maven-dependency
			List<String> installerIdsToDisplay = List.of(ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID,
					ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID);
			if (!installerIdsToDisplay.contains(mavenNode.path(ProductJsonConstants.ID).asText())) {
				continue;
			}

			// Extract repository URL
			JsonNode repositoriesNode = dataNode.path(ProductJsonConstants.REPOSITORIES);
			repoUrl = repositoriesNode.get(0).path(ProductJsonConstants.URL).asText();

			// Process projects
			if (dataNode.has(ProductJsonConstants.PROJECTS)) {
				extractMavenArtifactFromJsonNode(dataNode, false, artifacts);
			}

			// Process dependencies
			if (dataNode.has(ProductJsonConstants.DEPENDENCIES)) {
				extractMavenArtifactFromJsonNode(dataNode, true, artifacts);
			}
		}
		return artifacts;
	}

	public InputStream extractedContentStream(GHContent content) {
		try {
			return content.read();
		} catch (IOException | NullPointerException e) {
			log.warn("Can not read the current content: {}", e.getMessage());
			return null;
		}
	}

	public void extractMavenArtifactFromJsonNode(JsonNode dataNode, boolean isDependency,
			List<MavenArtifact> artifacts) {
		String nodeName = ProductJsonConstants.PROJECTS;
		if (isDependency) {
			nodeName = ProductJsonConstants.DEPENDENCIES;
		}
		JsonNode dependenciesNode = dataNode.path(nodeName);
		for (JsonNode dependencyNode : dependenciesNode) {
			MavenArtifact artifact = createArtifactFromJsonNode(dependencyNode, repoUrl, isDependency);
			artifacts.add(artifact);
		}
	}

	public MavenArtifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
		MavenArtifact artifact = new MavenArtifact();
		artifact.setRepoUrl(repoUrl);
		artifact.setIsDependency(isDependency);
		artifact.setGroupId(node.path(ProductJsonConstants.GROUP_ID).asText());
		artifact.setArtifactId(node.path(ProductJsonConstants.ARTIFACT_ID).asText());
		artifact.setType(node.path(ProductJsonConstants.TYPE).asText());
		artifact.setIsProductArtifact(true);
		return artifact;
	}

	@Override
	public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
		try {
			return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
		} catch (IOException e) {
			log.error("Cannot Get Content From File Directory", e);
			return null;
		}
	}

	public GHOrganization getOrganization() throws IOException {
		if (organization == null) {
			organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
		}
		return organization;
	}

	@Override
	public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
		return getOrganization().getRepository(repoName).listTags().toList();
	}

	@Override
	public ProductModuleContent getReadmeAndProductContentsFromTag(GHRepository ghRepository, String tag) {
		ProductModuleContent productModuleContent = new ProductModuleContent();
		try {
			List<GHContent> contents = getProductFolderContents(ghRepository, tag);
			productModuleContent.setTag(tag);
			getProductJsonContent(productModuleContent, contents);
			List<GHContent> readmeFiles = contents.stream().filter(GHContent::isFile)
					.filter(content -> GitHubConstants.PRODUCT_README_FILES.contains(content.getName()))
					.collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(readmeFiles)) {
				for (GHContent readmeFile : readmeFiles) {
					String readmeContents = new String(readmeFile.read().readAllBytes());
					if (hasImageDirectives(readmeContents)) {
						readmeContents = updateImagesWithDownloadUrl(contents, readmeContents);
					}
					String locale = getReadmeFileLocale(readmeFile.getName());
					getExtractedPartsOfReadme(productModuleContent, readmeContents, locale);
				}
			}
		} catch (Exception e) {
			log.error("Cannot get product.json and README file's content {}", e);
			return null;
		}
		return productModuleContent;
	}

	private String getReadmeFileLocale(String readmeFile) {
		String result = StringUtils.EMPTY;
		Pattern pattern = Pattern.compile(GitHubConstants.README_FILE_LOCALE_REGEX);
		Matcher matcher = pattern.matcher(readmeFile);
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	private void getProductJsonContent(ProductModuleContent productModuleContent, List<GHContent> contents)
			throws IOException {
		String productJsonContents;
		GHContent productJsonFile = contents.stream().filter(GHContent::isFile)
				.filter(content -> ProductJsonConstants.PRODUCT_JSON_FILE.equals(content.getName())).findFirst()
				.orElse(null);
		if (Objects.nonNull(productJsonFile)) {
			productJsonContents = new String(productJsonFile.read().readAllBytes());
			JsonNode rootNode = MAPPER.readTree(productJsonContents);
			JsonNode installersNode = rootNode.path(ProductJsonConstants.INSTALLERS);
			for (JsonNode installerNode : installersNode) {
				if (installerNode.path(ProductJsonConstants.ID).asText()
						.endsWith(ProductJsonConstants.DEPENDENCY_SUFFIX)) {
					JsonNode dataNode = installerNode.path(ProductJsonConstants.DATA);
					JsonNode dependenciesNode = dataNode.path(ProductJsonConstants.DEPENDENCIES);
					productModuleContent.setIsDependency(Boolean.TRUE);
					productModuleContent
							.setGroupId(dependenciesNode.get(0).path(ProductJsonConstants.GROUP_ID).asText());
					productModuleContent
							.setArtifactId(dependenciesNode.get(0).path(ProductJsonConstants.ARTIFACT_ID).asText());
					productModuleContent.setType(dependenciesNode.get(0).path(ProductJsonConstants.TYPE).asText());
					productModuleContent.setName(GitHubUtils.convertArtifactIdToName(productModuleContent.getArtifactId()));
				}
			}
		}
	}

	public String updateImagesWithDownloadUrl(List<GHContent> contents, String readmeContents) throws IOException {
		Map<String, String> imageUrls = new HashMap<>();
		GHContent productImage = contents.stream().filter(GHContent::isFile)
				.filter(content -> content.getName().toLowerCase().matches(IMAGE_EXTENSION)).findAny()
				.orElse(null);
		if (Objects.nonNull(productImage)) {
			imageUrls.put(productImage.getName(), productImage.getDownloadUrl());
		} else {
			GHContent imageFolder = contents.stream().filter(GHContent::isDirectory)
					.filter(content -> ReadmeConstants.IMAGES.equals(content.getName())).findFirst().orElse(null);
			if (Objects.nonNull(imageFolder)) {
				for (GHContent imageContent : imageFolder.listDirectoryContent().toList()) {
					imageUrls.put(imageContent.getName(), imageContent.getDownloadUrl());
				}
			}
		}
		for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
			String imageUrlPattern = "\\(([^)]*?" + Pattern.quote(entry.getKey()) + "[^)]*?)\\)";
			readmeContents = readmeContents.replaceAll(imageUrlPattern, "(" + entry.getValue() + ")");

		}
		return readmeContents;
	}

	// Cover some cases including when demo and setup parts switch positions or
	// missing one of them
	public void getExtractedPartsOfReadme(ProductModuleContent productModuleContent, String readmeContents, String locale) {
		String[] parts = readmeContents.split(DEMO_SETUP_TITLE);
		boolean hasDemoPart = readmeContents.contains(ReadmeConstants.DEMO_PART);
		boolean hasSetupPart = readmeContents.contains(ReadmeConstants.SETUP_PART);
		String description = Strings.EMPTY;
		String setup = Strings.EMPTY;
		String demo = Strings.EMPTY;

		if (parts.length > 0){
			description = removeFirstLine(parts[0]);
		}

		if (hasDemoPart && hasSetupPart) {
			if (isDemoPlaceBeforeSetupPart(readmeContents)) {
				demo = parts[1];
				setup = parts[2];
			} else {
				setup = parts[1];
				demo = parts[2];
			}
		} else if (hasDemoPart) {
			demo = parts[1];
		} else if (hasSetupPart) {
			setup = parts[1];
		}

		productModuleContent.setDemo(demo.trim());
		productModuleContent.setSetup(setup.trim());
		setDescriptionWithLocale(productModuleContent, description, locale);
	}

	private void setDescriptionWithLocale(ProductModuleContent productModuleContent, String description,
			String locale) {
		if (productModuleContent.getDescription() == null) {
			productModuleContent.setDescription(new HashMap<>());
		}
		if (StringUtils.isEmpty(locale)) {
			productModuleContent.getDescription().put(Language.EN.getValue(), description);
		} else {
			productModuleContent.getDescription().put(locale.toLowerCase(), description);
		}
	}

	private static boolean isDemoPlaceBeforeSetupPart(String readmeContents) {
		return readmeContents.indexOf(ReadmeConstants.DEMO_PART) < readmeContents
				.indexOf(ReadmeConstants.SETUP_PART);
	}

	private List<GHContent> getProductFolderContents(GHRepository ghRepository, String tag) throws IOException {
		return ghRepository.getDirectoryContent(CommonConstants.SLASH, tag).stream().filter(GHContent::isDirectory)
				.filter(content -> content.getName().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX))
				.flatMap(content -> {
					try {
						return content.listDirectoryContent().toList().stream();
					} catch (Exception e) {
						return Stream.empty();
					}
				}).toList();
	}

	private boolean hasImageDirectives(String readmeContents) {
		Pattern pattern = Pattern.compile(IMAGE_EXTENSION);
		Matcher matcher = pattern.matcher(readmeContents);
		return matcher.find();
	}

	private String removeFirstLine(String text) {
		if (text.isBlank()) {
			return Strings.EMPTY;
		}
		int index = text.indexOf(StringUtils.LF);
		return index != -1 ? text.substring(index + 1).trim() : Strings.EMPTY;
	}
}
