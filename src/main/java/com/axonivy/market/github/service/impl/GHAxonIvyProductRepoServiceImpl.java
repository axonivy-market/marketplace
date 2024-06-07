package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl extends AbstractGithubService implements GHAxonIvyProductRepoService {
    private GHOrganization organization;

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
