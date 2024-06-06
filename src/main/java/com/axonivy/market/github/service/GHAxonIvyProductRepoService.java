package com.axonivy.market.github.service;

import org.kohsuke.github.GHContent;

public interface GHAxonIvyProductRepoService {

  public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);
}
