package com.axonivy.market.github.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHTag;

import java.util.List;
import java.util.Map;

public interface GHAxonIvyMarketRepoService {

    Map<String, List<GHContent>> fetchAllMarketItems();

    GHCommit getLastCommit();

    GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);

    List<GHTag> getTagsFromRepoName(String repoName);
}
