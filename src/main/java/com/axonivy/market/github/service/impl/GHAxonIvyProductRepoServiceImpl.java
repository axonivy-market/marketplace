package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl extends AbstractGithubService implements GHAxonIvyProductRepoService {
    ObjectMapper mapper = new ObjectMapper();
    private GHOrganization organization;

    @Override
    public List<MavenArtifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(content.getContent());
        List<MavenArtifact> artifacts = new ArrayList<>();

        JsonNode installersNode = rootNode.path(ProductJsonConstants.INSTALLERS);

        for (JsonNode installerNode : installersNode) {
            JsonNode dataNode = installerNode.path(ProductJsonConstants.DATA);

            // Extract repository URL
            JsonNode repositoriesNode = dataNode.path(ProductJsonConstants.REPOSITORIES);
            String repoUrl = repositoriesNode.get(0).path(ProductJsonConstants.URL).asText();

            // Process projects
            if (dataNode.has(ProductJsonConstants.PROJECTS)) {
                JsonNode projectsNode = dataNode.path(ProductJsonConstants.PROJECTS);
                for (JsonNode projectNode : projectsNode) {
                    MavenArtifact artifact = createArtifactFromJsonNode(projectNode, repoUrl, false);
                    artifacts.add(artifact);
                }
            }

            // Process dependencies
            if (dataNode.has(ProductJsonConstants.DEPENDENCIES)) {
                JsonNode dependenciesNode = dataNode.path(ProductJsonConstants.DEPENDENCIES);
                for (JsonNode dependencyNode : dependenciesNode) {
                    MavenArtifact artifact = createArtifactFromJsonNode(dependencyNode, repoUrl, true);
                    artifacts.add(artifact);
                }
            }
        }
        return artifacts;
    }

    private MavenArtifact createArtifactFromJsonNode(JsonNode node, String repoUrl, boolean isDependency) {
        MavenArtifact artifact = new MavenArtifact();
        artifact.setRepoUrl(repoUrl);
        artifact.setIsDependency(isDependency);
        artifact.setGroupId(node.path(ProductJsonConstants.GROUP_ID).asText());
        artifact.setName(convertArtifactIdToName(artifact.getArtifactId()));
        artifact.setArtifactId(node.path(ProductJsonConstants.ARTIFACT_ID).asText());
        artifact.setType(node.path(ProductJsonConstants.TYPE).asText());
        return artifact;
    }

    private String convertArtifactIdToName(String artifactId) {
        if (StringUtils.isNotBlank(artifactId)) {
            return artifactId.replace(MavenConstants.ARTIFACT_ID_SEPARATOR, MavenConstants.ARTIFACT_NAME_SEPARATOR);
        }
        return StringUtils.EMPTY;
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
            organization = getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
    }

    @Override
    public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
        return getOrganization().getRepository(repoName).listTags().toList();
    }
}
