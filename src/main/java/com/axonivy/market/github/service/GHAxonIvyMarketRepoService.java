package com.axonivy.market.github.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHTag;

import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;

import com.axonivy.market.github.model.GitHubFile;

public interface GHAxonIvyMarketRepoService {

    Map<String, List<GHContent>> fetchAllMarketItems();


    GHContent getContentFromGHRepo(String repoName, String filePath);

  GHContent getGHContent(String path);
}
