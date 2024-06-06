package com.axonivy.market.github.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHTag;

import java.util.List;
import java.util.Map;

public interface GHAxonIvyMarketRepoService {

    Map<String, List<GHContent>> fetchAllMarketItems();


    GHContent getContentFromGHRepo(String repoName, String filePath);

    List<GHTag> getTagsFromRepoName(String repoName);

    GHCommit getLastCommit();
}
