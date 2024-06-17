package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHCompare;
import com.axonivy.market.github.service.GithubService;
import com.axonivy.market.github.util.GithubUtils;


@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl implements GHAxonIvyMarketRepoService {
    private static final String DEFAULT_BRANCH = "master";
    private static final LocalDateTime INITIAL_COMMIT_DATE = LocalDateTime.of(2020, 10, 30, 0, 0);
    private GHOrganization organization;
    private GHRepository repository;

    private final GithubService githubService;

    public GHAxonIvyMarketRepoServiceImpl(GithubService githubService) {
        this.githubService = githubService;
    }

    @Override
    public Map<String, List<GHContent>> fetchAllMarketItems() {
        Map<String, List<GHContent>> ghContentMap = new HashMap<>();
        try {
            List<GHContent> directoryContent = githubService.getDirectoryContent(getRepository(),
                    GitHubConstants.AXONIVY_MARKETPLACE_PATH);
            for (var content : directoryContent) {
                extractFileInDirectoryContent(content, ghContentMap);
            }
        } catch (Exception e) {
            log.error("Cannot fetch GHContent: ", e);
        }
        return ghContentMap;
    }

    private void extractFileInDirectoryContent(GHContent content, Map<String, List<GHContent>> ghContentMap)
            throws IOException {
        if (content != null && content.isDirectory()) {
            for (var childContent : GithubUtils.mapPagedIteratorToList(content.listDirectoryContent())) {
                if (childContent.isFile()) {
                    var contents = ghContentMap.getOrDefault(content.getPath(), new ArrayList<>());
                    contents.add(childContent);
                    ghContentMap.putIfAbsent(content.getPath(), contents);
                } else {
                    extractFileInDirectoryContent(childContent, ghContentMap);
                }
            }
        }
    }

    @Override
    public GHCommit getLastCommit(long lastCommitTime) {
        if (lastCommitTime == 0l) {
            lastCommitTime = INITIAL_COMMIT_DATE.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        try {
            GHCommitQueryBuilder commitBuilder = createQueryCommitsBuilder(lastCommitTime);
            return GithubUtils.mapPagedIteratorToList(commitBuilder.list()).stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.error("Cannot query GHCommit: ", e);
        }
        return null;
    }

    private GHCommitQueryBuilder createQueryCommitsBuilder(long lastCommitTime) {
        return getRepository().queryCommits().since(lastCommitTime).from(DEFAULT_BRANCH);
    }

    @Override
    public List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1) {
        Map<String, GitHubFile> githubFileMap = new HashMap<>();
        try {
            GHCompare compareResult = getRepository().getCompare(fromSHA1, toSHA1);
            for (var commit : GithubUtils.mapPagedIteratorToList(compareResult.listCommits())) {
                var listFiles = commit.listFiles();
                if (listFiles == null) {
                    continue;
                }
                GithubUtils.mapPagedIteratorToList(listFiles).forEach(file -> {
                    String fullPathName = file.getFileName();
                    if (FileType.of(fullPathName) != null) {
                        var githubFile = new GitHubFile();
                        githubFile.setFileName(fullPathName);
                        githubFile.setPath(file.getRawUrl().getPath());
                        githubFile.setStatus(FileStatus.of(file.getStatus()));
                        githubFile.setType(FileType.of(fullPathName));
                        githubFile.setPreviousFilename(file.getPreviousFilename());
                        githubFileMap.put(fullPathName, githubFile);
                    }
                });
            }
        } catch (IOException e) {
            log.error("Cannot get GH compare: ", e);
        }
        return new ArrayList<>(githubFileMap.values());
    }

    private GHOrganization getOrganization() throws IOException {
        if (organization == null) {
            organization = githubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
    }

    @Override
    public GHRepository getRepository() {
        if (repository == null) {
            try {
                repository = getOrganization().getRepository(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
            } catch (IOException e) {
                log.error("Get AxonIvy Market repo failed: ", e);
            }
        }
        return repository;
    }

}