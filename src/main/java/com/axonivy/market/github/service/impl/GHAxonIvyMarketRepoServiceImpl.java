package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;

import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {
  
  private GHOrganization organization;
  private GHRepository repository;

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
    try {
      var directoryContent = getDirectoryContent(getRepository(), "market");
      for (var content : directoryContent) {
        extractFileOfContent(content, ghContentMap);
      }
    } catch (Exception e) {
      log.error("Cannot fetch GH Content", e);
    }
    return ghContentMap;
  }

  private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
    if (content.isDirectory()) {
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
  public String getLastCommit() {
    // TODO Auto-generated method stub
    try {
      getRepository().queryCommits().since(null);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = getOrganization("axonivy-market");
    }
    return organization;
  }

  public GHRepository getRepository() throws IOException {
    if (repository == null) {
      repository = getOrganization().getRepository("market");
    }
    return repository;
  }
}
