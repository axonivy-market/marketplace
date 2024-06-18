package com.axonivy.market.github.service;

import com.axonivy.market.github.model.GitHubFile;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;

import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHRepository;

public interface GHAxonIvyMarketRepoService {

  public Map<String, List<GHContent>> fetchAllMarketItems();

  public GHCommit getLastCommit(long lastCommitTime);

  public List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1);

  public GHRepository getRepository();
}
