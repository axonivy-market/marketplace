package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl extends AbstractGithubService implements GHAxonIvyProductRepoService {
    ObjectMapper mapper = new ObjectMapper();
    private GHOrganization organization;

    @Override
    public List<MavenArtifact> convertProductJsonToMavenProductInfo(GHContent content) throws IOException {
        List<Map<String, Object>> installers = mapper.readValue(
                content.read().readAllBytes(),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );

        List<MavenArtifact> artifacts = new ArrayList<>();
        for (Map<String, Object> installer : installers) {
            String installerId = (String) installer.get(ProductJsonConstants.ID);
            Map<String, Object> data = (Map<String, Object>) installer.get(ProductJsonConstants.DATA);

            List<Map<String, String>> repositories = (List<Map<String, String>>) data.get(ProductJsonConstants.REPOSITORIES);
            String repoUrl = repositories.get(0).get(ProductJsonConstants.URL);

            if (ProductJsonConstants.MAVEN_IMPORT.equals(installerId)) {
                List<Map<String, String>> projects = (List<Map<String, String>>) data.get(ProductJsonConstants.PROJECTS);
                for (Map<String, String> project : projects) {
                    artifacts.add(new MavenArtifact(
                            repoUrl,
                            project.get(ProductJsonConstants.ARTIFACT_ID).replace(MavenConstants.ARTIFACT_ID_SEPARATOR, MavenConstants.ARTIFACT_NAME_SEPARATOR),
                            project.get(ProductJsonConstants.GROUP_ID),
                            project.get(ProductJsonConstants.ARTIFACT_ID),
                            project.get(ProductJsonConstants.TYPE)
                    ));
                }
            } else if (ProductJsonConstants.MAVEN_DEPENDENCY.equals(installerId)) {
                List<Map<String, String>> dependencies = (List<Map<String, String>>) data.get(ProductJsonConstants.DEPENDENCIES);
                for (Map<String, String> dependency : dependencies) {
                    artifacts.add(new MavenArtifact(
                            repoUrl,
                            dependency.get(ProductJsonConstants.ARTIFACT_ID).replace(MavenConstants.ARTIFACT_ID_SEPARATOR, MavenConstants.ARTIFACT_NAME_SEPARATOR),
                            dependency.get(ProductJsonConstants.GROUP_ID),
                            dependency.get(ProductJsonConstants.ARTIFACT_ID),
                            dependency.get(ProductJsonConstants.TYPE)
                    ));
                }
            }
        }

        return artifacts;
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
