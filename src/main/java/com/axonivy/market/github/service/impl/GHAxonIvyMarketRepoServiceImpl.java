package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.AbstractGithubService;
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

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {
    private static final String DEFAULT_BRANCH = "master";
    private static final LocalDateTime INITIAL_COMMIT_DATE = LocalDateTime.of(2020, 10, 30, 0, 0);
    private GHOrganization organization;
    private GHRepository repository;

    @Override
    public Map<String, List<GHContent>> fetchAllMarketItems() {
        Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
        try {
            var directoryContent = getDirectoryContent(getRepository(), GitHubConstants.AXONIVY_MARKETPLACE_PATH);
            for (var content : directoryContent) {
                extractFileOfContent(content, ghContentMap);
            }
        } catch (Exception e) {
            log.error("Cannot fetch GHContent", e);
        }
        return ghContentMap;
    }

    private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
        if (content.isDirectory() && content.getName().equals("docuware-connector")) {
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

    @Override
    public GHCommit getLastCommit(long lastCommitTime) {
        GHCommit lastCommit = null;
        if (lastCommitTime == 0L) {
            lastCommitTime = INITIAL_COMMIT_DATE.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        try {
            var lastCommits = getRepository().queryCommits().since(lastCommitTime).from(DEFAULT_BRANCH).list().iterator();
            if (lastCommits.hasNext()) {
                lastCommit = lastCommits.next();
            }
        } catch (Exception e) {
            log.error("Cannot query GHCommit: ", e);
        }
        return lastCommit;
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
    public List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1) {
        Map<String, GitHubFile> githubFileMap = new HashMap<>();
        try {
            var compareResult = getRepository().getCompare(fromSHA1, toSHA1);
            for (var commit : compareResult.listCommits()) {
                for (var file : commit.listFiles()) {
                    var fullPathName = file.getFileName();
                    if (FileType.of(fullPathName) == null) {
                        continue;
                    }
                    var githubFile = new GitHubFile();
                    githubFile.setFileName(fullPathName);
                    githubFile.setPath(file.getRawUrl().getPath());
                    githubFile.setStatus(FileStatus.of(file.getStatus()));
                    githubFile.setType(FileType.of(fullPathName));
                    githubFile.setPreviousFilename(file.getPreviousFilename());
                    githubFileMap.put(fullPathName, githubFile);
                }
            }
        } catch (IOException e) {
            log.error("Cannot get GH compare: ", e);
        }
        return new ArrayList<>(githubFileMap.values());
    }

    @Override
    public GHContent getGHContent(String path) {
        try {
            return getRepository().getFileContent(path);
        } catch (IOException e) {
            log.error("Cannot get GHContent by path {}: {}", path, e);
        }
        return null;
    }

}
