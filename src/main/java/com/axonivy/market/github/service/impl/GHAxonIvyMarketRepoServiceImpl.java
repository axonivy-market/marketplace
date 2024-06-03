package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;

import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
    try {
      var marketOrg = getOrganization("axonivy-market");
      var directoryContent = getDirectoryContent(marketOrg.getRepository("market"), "market/connector");
      for (var content : directoryContent) {
        if (content.getName().equals("adobe-acrobat-sign-connector")) {
          log.warn(content.getName());
          extractFileOfContent(content, ghContentMap);
        }
      }
    } catch (Exception e) {
      log.error("Cannot fetch GH Content", e);
    }
    return ghContentMap;
  }

  private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
    if (content.isDirectory()) {
      var listOfContent = content.listDirectoryContent();
      log.warn("found nha");
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
}
