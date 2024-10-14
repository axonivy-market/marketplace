package com.axonivy.market.github.service;

import com.axonivy.market.github.model.GitHubFile;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.util.List;
import java.util.Map;

public interface GHAxonIvyMarketRepoService {

  Map<String, List<GHContent>> fetchAllMarketItems();

  GHCommit getLastCommit(long lastCommitTime);

  List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1);

  GHRepository getRepository();

  List<GHContent> getMarketItemByPath(String itemPath);
}
