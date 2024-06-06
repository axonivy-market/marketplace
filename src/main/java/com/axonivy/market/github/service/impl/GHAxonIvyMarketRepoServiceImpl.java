package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {

    private final GithubRepoMetaRepository repoMetaRepository;
    private GHOrganization organization;
    private GHRepository repository;

    public GHAxonIvyMarketRepoServiceImpl(GithubRepoMetaRepository repoMetaRepository) {
        this.repoMetaRepository = repoMetaRepository;
    }

    @Override
    public Map<String, List<GHContent>> fetchAllMarketItems() {
        // TODO
        Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
        try {
            var directoryContent = getDirectoryContent(getRepository(), GitHubConstants.AXONIVY_MARKETPLACE_PATH);
            for (var content : directoryContent) {
                extractFileOfContent(content, ghContentMap);
            }
        } catch (Exception e) {
            log.error("Cannot fetch GH Content", e);
        }
        return ghContentMap;
    }

    private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
        if (content.isDirectory()) {
            var listOfContent = content.listDirectoryContent();
            for (var childContent : listOfContent.toList()) {
                if (childContent.isFile()) {
                    var contents = ghContentMap.getOrDefault(content.getPath(), new ArrayList<GHContent>());
                    contents.add(childContent);
                    ghContentMap.putIfAbsent(content.getPath(), contents);
                } else {
                    extractFileOfContent(childContent, ghContentMap);
                }
            }
        }
    }

    public GHOrganization getOrganization() throws IOException {
        if (organization == null) {
            organization = getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
    }

    public GHRepository getRepository() throws IOException {
        if (repository == null) {
            repository = getOrganization().getRepository(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
        }
        return repository;
    }

    @Override
    public GHContent getContentFromGHRepo(String repoName, String filePath) {
        try {
            return getOrganization().getRepository(repoName).getFileContent(filePath);
        } catch (IOException e) {
            log.error("Cannot Get Content From File Directory", e);
            return null;
        }
    }

    @Override
    public List<GHTag> getTagsFromRepoName(String repoName) {
        try {
            return getOrganization().getRepository(repoName).listTags().toList();
        } catch (IOException e) {
            log.error("Cannot Get Tag From Current Repo", e);
            return Collections.emptyList();
        }
    }

}
