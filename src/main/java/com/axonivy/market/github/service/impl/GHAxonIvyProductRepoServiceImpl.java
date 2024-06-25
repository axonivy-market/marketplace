package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.axonivy.market.model.ReadmeModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import lombok.extern.log4j.Log4j2;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;
import com.axonivy.market.github.service.GitHubService;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
    private GHOrganization organization;

    private final GitHubService gitHubService;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String SLASH = "/";
    public static final String IMAGES_FOLDER = "images";
    public static final String PRODUCT_FOLDER_SUFFIX = "-product";
    public static final String README_FILE = "README.md";
    public static final String PRODUCT_JSON_FILE = "product.json";

    public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
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

    @Override
    public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
        return getOrganization().getRepository(repoName).listTags().toList();
    }

    @Override
    public ReadmeModel getReadmeAndProductContentsFromTag(String repoName, String tag) {
        ReadmeModel readmeModel = new ReadmeModel();
        try {
            List<GHContent> contents = getRepoContents(repoName, tag);
            readmeModel.setTag(tag);
            getProductJsonContent(readmeModel, contents);
            Optional<GHContent> readmeFile = contents.stream()
                    .filter(GHContent::isFile)
                    .filter(content -> README_FILE.equals(content.getName()))
                    .findFirst();
            if (readmeFile.isPresent()) {
                String readmeContents = new String(readmeFile.get().read().readAllBytes());
                if (containsImageDirectives(readmeContents)) {
                    readmeContents = updateImagesWithDownloadUrl(contents, readmeContents);
                    getExtractedPartsOfReadme(readmeModel, readmeContents);
                } else {
                    getExtractedPartsOfReadme(readmeModel, readmeContents);
                }
            }
        } catch (Exception e) {
            log.error("Cannot get product.json and README file's content {}", e);
            return null;
        }
        return readmeModel;
    }

    private void getProductJsonContent(ReadmeModel readmeModel, List<GHContent> contents) throws IOException {
        String productJsonContents;
        Optional<GHContent> productJsonFile = contents.stream().filter(GHContent::isFile).filter(content -> PRODUCT_JSON_FILE.equals(content.getName())).findFirst();
        if (productJsonFile.isPresent()) {
            productJsonContents = new String(productJsonFile.get().read().readAllBytes());
            JsonNode rootNode = MAPPER.readTree(productJsonContents);
            JsonNode installersNode = rootNode.path("installers");
            for (JsonNode installerNode : installersNode) {
                if (installerNode.path("id").asText().endsWith("-dependency")) {
                    JsonNode dataNode = installerNode.path("data");
                    JsonNode dependenciesNode = dataNode.path("dependencies");
                    readmeModel.setIsDependency(Boolean.TRUE);
                    readmeModel.setGroupId(dependenciesNode.get(0).path("groupId").asText());
                    readmeModel.setArtifactId(dependenciesNode.get(0).path("artifactId").asText());
                    readmeModel.setType(dependenciesNode.get(0).path("type").asText());
                    readmeModel.setName(StringUtils.isBlank(readmeModel.getArtifactId()) ? StringUtils.EMPTY : WordUtils.capitalize(readmeModel.getArtifactId().replaceAll("-", " ")));
                }
            }
        }
    }

    public String updateImagesWithDownloadUrl(List<GHContent> contents, String readmeContents) throws IOException {
        String imagePrefix = "";
        Map<String, String> imageUrls = new HashMap<>();
        Optional<GHContent> productImage = contents.stream()
                .filter(GHContent::isFile)
                .filter(content -> content.getName().toLowerCase().matches(".+\\.(jpeg|jpg|png)"))
                .findAny();
        if (productImage.isPresent()) {
            imageUrls.put(productImage.get().getName(), productImage.get().getDownloadUrl());
        } else {
            Optional<GHContent> imageFolder = contents.stream()
                    .filter(GHContent::isDirectory)
                    .filter(content -> IMAGES_FOLDER.equals(content.getName()))
                    .findFirst();
            if (imageFolder.isPresent()) {
                imagePrefix = IMAGES_FOLDER.concat(SLASH);
                for (GHContent imageContent : imageFolder.get().listDirectoryContent()) {
                    imageUrls.put(imageContent.getName(), imageContent.getDownloadUrl());
                }
            }
        }
        for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
            readmeContents = readmeContents.replaceAll(imagePrefix + Pattern.quote(entry.getKey()), entry.getValue());
        }
        return readmeContents;
    }

    // Cover some cases including when demo and setup parts switch positions or missing one of them
    public void getExtractedPartsOfReadme(ReadmeModel readmeModel, String readmeContents) {
        String[] parts = readmeContents.split("(?i)## Demo|## Setup");
        boolean hasDemoPart = readmeContents.contains("## Demo");
        boolean hasSetupPart = readmeContents.contains("## Setup");
        String setup = "";
        String demo = "";
        if (hasDemoPart && hasSetupPart) {
            if (readmeContents.indexOf("## Demo") < readmeContents.indexOf("## Setup")) {
                demo = parts.length > 1 ? parts[1].trim() : StringUtils.EMPTY;
                setup = parts.length > 2 ? parts[2].trim() : StringUtils.EMPTY;
            } else {
                setup = parts.length > 1 ? parts[1].trim() : StringUtils.EMPTY;
                demo = parts.length > 2 ? parts[2].trim() : StringUtils.EMPTY;
            }
        } else if (hasDemoPart) {
            demo = parts.length > 1 ? parts[1].trim() : StringUtils.EMPTY;
        } else if (hasSetupPart) {
            setup = parts.length > 1 ? parts[1].trim() : StringUtils.EMPTY;
        }
        readmeModel.setDescription(parts.length > 0 ? removeFirstLine(parts[0].trim()) : StringUtils.EMPTY);
        readmeModel.setDemo(demo);
        readmeModel.setSetup(setup);
    }

    private List<GHContent> getRepoContents(String repoName, String tag) throws IOException {
        return gitHubService.getRepository(repoName)
                .getDirectoryContent(SLASH, tag)
                .stream()
                .filter(GHContent::isDirectory)
                .filter(content -> content.getName().endsWith(PRODUCT_FOLDER_SUFFIX))
                .flatMap(content -> {
                    try {
                        return content.listDirectoryContent().toList().stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    private boolean containsImageDirectives(String readmeContents) {
        Pattern pattern = Pattern.compile("images/(.*?)");
        Matcher matcher = pattern.matcher(readmeContents);
        return matcher.find();
    }

    private String removeFirstLine(String text) {
        if (text == null || text.isEmpty()) {
            return StringUtils.EMPTY;
        }
        int index = text.indexOf("\n");
        if (index != -1) {
            return text.substring(index + 1).trim();
        } else {
            return StringUtils.EMPTY;
        }
    }
}
