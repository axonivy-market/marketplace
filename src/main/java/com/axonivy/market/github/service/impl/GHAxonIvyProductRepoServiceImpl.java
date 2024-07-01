package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.*;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReleaseTagConstants;
import com.axonivy.market.entity.ReadmeProductContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import lombok.extern.log4j.Log4j2;

import org.apache.logging.log4j.util.Strings;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import com.axonivy.market.github.service.GitHubService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
	private GHOrganization organization;
	private final GitHubService gitHubService;
	private String repoUrl;
	private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String DEMO_SETUP_TITLE = "(?i)## Demo|## Setup";

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
    @Override
    public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
        try {
            return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
        } catch (IOException e) {
            log.error("Cannot Get Content From File Directory", e);
            return null;
        }
    }

    private GHOrganization getOrganization() throws IOException {
        if (organization == null) {
            organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
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
    public ReadmeProductContent getReadmeAndProductContentsFromTag(GHRepository ghRepository, String tag) {
        ReadmeProductContent readmeProductContent = new ReadmeProductContent();
        try {
            List<GHContent> contents = getRepoContents(ghRepository, tag);
            readmeProductContent.setTag(tag);
            getProductJsonContent(readmeProductContent, contents);
            GHContent readmeFile = contents.stream()
                    .filter(GHContent::isFile)
                    .filter(content -> ReleaseTagConstants.README_FILE.equals(content.getName()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(readmeFile)) {
                String readmeContents = new String(readmeFile.read().readAllBytes());
                if (containsImageDirectives(readmeContents)) {
                    readmeContents = updateImagesWithDownloadUrl(contents, readmeContents);
                    getExtractedPartsOfReadme(readmeProductContent, readmeContents);
                } else {
                    getExtractedPartsOfReadme(readmeProductContent, readmeContents);
                }
            }
        } catch (Exception e) {
            log.error("Cannot get product.json and README file's content {}", e);
            return null;
        }
        return readmeProductContent;
    }

    private void getProductJsonContent(ReadmeProductContent readmeProductContent, List<GHContent> contents) throws IOException {
        String productJsonContents;
        GHContent productJsonFile = contents.stream().filter(GHContent::isFile).filter(content -> ReleaseTagConstants.PRODUCT_JSON_FILE.equals(content.getName())).findFirst().orElse(null);
        if (Objects.nonNull(productJsonFile)) {
            productJsonContents = new String(productJsonFile.read().readAllBytes());
            JsonNode rootNode = MAPPER.readTree(productJsonContents);
            JsonNode installersNode = rootNode.path(ReleaseTagConstants.INSTALLERS);
            for (JsonNode installerNode : installersNode) {
                if (installerNode.path(ReleaseTagConstants.ID).asText().endsWith(ReleaseTagConstants.DEPENDENCY_SUFFIX)) {
                    JsonNode dataNode = installerNode.path(ReleaseTagConstants.DATA);
                    JsonNode dependenciesNode = dataNode.path(ReleaseTagConstants.DEPENDENCIES);
                    readmeProductContent.setIsDependency(Boolean.TRUE);
                    readmeProductContent.setGroupId(dependenciesNode.get(0).path(ReleaseTagConstants.GROUP_ID).asText());
                    readmeProductContent.setArtifactId(dependenciesNode.get(0).path(ReleaseTagConstants.ARTIFACT_ID).asText());
                    readmeProductContent.setType(dependenciesNode.get(0).path(ReleaseTagConstants.TYPE).asText());
                    readmeProductContent.setName(convertArtifactIdToName(readmeProductContent.getArtifactId()));
                }
            }
        }
    }

    public String updateImagesWithDownloadUrl(List<GHContent> contents, String readmeContents) throws IOException {
        Map<String, String> imageUrls = new HashMap<>();
        GHContent productImage = contents.stream()
                .filter(GHContent::isFile)
                .filter(content -> content.getName().toLowerCase().matches(".+\\.(jpeg|jpg|png)"))
                .findAny().orElse(null);
        if (Objects.nonNull(productImage)) {
            imageUrls.put(productImage.getName(), productImage.getDownloadUrl());
        } else {
            GHContent imageFolder = contents.stream()
                    .filter(GHContent::isDirectory)
                    .filter(content -> ReleaseTagConstants.IMAGES.equals(content.getName()))
                    .findFirst().orElse(null);
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

    // Cover some cases including when demo and setup parts switch positions or missing one of them
    public void getExtractedPartsOfReadme(ReadmeProductContent readmeProductContent, String readmeContents) {
        String[] parts = readmeContents.split(DEMO_SETUP_TITLE);
        boolean hasDemoPart = readmeContents.contains(ReleaseTagConstants.DEMO_PART);
        boolean hasSetupPart = readmeContents.contains(ReleaseTagConstants.SETUP_PART);
        String setup = Strings.EMPTY;
        String demo = Strings.EMPTY;
        if (hasDemoPart && hasSetupPart) {
            if (readmeContents.indexOf(ReleaseTagConstants.DEMO_PART) < readmeContents.indexOf(ReleaseTagConstants.SETUP_PART)) {
                demo = parts.length > 1 ? parts[1].trim() : Strings.EMPTY;
                setup = parts.length > 2 ? parts[2].trim() : Strings.EMPTY;
            } else {
                setup = parts.length > 1 ? parts[1].trim() : Strings.EMPTY;
                demo = parts.length > 2 ? parts[2].trim() : Strings.EMPTY;
            }
        } else if (hasDemoPart) {
            demo = parts.length > 1 ? parts[1].trim() : Strings.EMPTY;
        } else if (hasSetupPart) {
            setup = parts.length > 1 ? parts[1].trim() : Strings.EMPTY;
        }
        readmeProductContent.setDescription(parts.length > 0 ? removeFirstLine(parts[0].trim()) : Strings.EMPTY);
        readmeProductContent.setDemo(demo);
        readmeProductContent.setSetup(setup);
    }

    private List<GHContent> getRepoContents(GHRepository ghRepository, String tag) throws IOException {
        return ghRepository.getDirectoryContent(CommonConstants.SLASH, tag)
                .stream()
                .filter(GHContent::isDirectory)
                .filter(content -> content.getName().endsWith(ReleaseTagConstants.PRODUCT_FOLDER_SUFFIX))
                .flatMap(content -> {
                    try {
                        return content.listDirectoryContent().toList().stream();
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                }).toList();
    }

    private boolean containsImageDirectives(String readmeContents) {
        Pattern pattern = Pattern.compile("(.*?).(jpeg|jpg|png)");
        Matcher matcher = pattern.matcher(readmeContents);
        return matcher.find();
    }

    private String removeFirstLine(String text) {
        if (text.isBlank()) {
            return Strings.EMPTY;
        }
        int index = text.indexOf(CommonConstants.NEW_LINE);
        return index != -1 ? text.substring(index + 1).trim(): Strings.EMPTY;
    }

    public String convertArtifactIdToName(String artifactId) {
        if (StringUtils.isBlank(artifactId)) {
            return Strings.EMPTY;
        }
        return Arrays.stream(artifactId.split(CommonConstants.DASH))
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(CommonConstants.SPACE));
    }
	@Override
	public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
		return getOrganization().getRepository(repoName).listTags().toList();
	}
}
