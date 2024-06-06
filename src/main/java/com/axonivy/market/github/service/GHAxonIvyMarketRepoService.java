package com.axonivy.market.github.service;

import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;

import com.axonivy.market.github.model.GitHubFile;

public interface GHAxonIvyMarketRepoService {

  Map<String, List<GHContent>> fetchAllMarketItems();

  GHCommit getLastCommit(long lastCommitTime);

  List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1);

  GHContent getGHContent(String path);
}
