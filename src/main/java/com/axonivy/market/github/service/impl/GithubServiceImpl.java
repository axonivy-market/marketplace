package com.axonivy.market.github.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import com.axonivy.market.github.service.GithubService;

@Service
public class GithubServiceImpl implements GithubService {

    private static final String GITHUB_TOKEN_FILE = "classpath:github.token";

    @Override
    public GitHub getGithub() throws IOException {
        File githubtoken = ResourceUtils.getFile(GITHUB_TOKEN_FILE);
        var token = Files.readString(githubtoken.toPath());
        return new GitHubBuilder().withOAuthToken(token).build();
    }

    @Override
    public GHOrganization getOrganization(String orgName) throws IOException {
        return getGithub().getOrganization(orgName);
    }

    @Override
    public List<GHContent> getDirectoryContent(GHRepository ghRepository, String path) throws IOException {
        Assert.notNull(ghRepository, "Repository must not be null");
        return ghRepository.getDirectoryContent(path);
    }

    @Override
    public GHRepository getRepository(String repositoryPath) throws IOException {
        return getGithub().getRepository(repositoryPath);
    }

    @Override
    public GHContent getGHContent(GHRepository ghRepository, String path) throws IOException {
        Assert.notNull(ghRepository, "Repository must not be null");
        return ghRepository.getFileContent(path);
    }
}
