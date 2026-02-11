package com.axonivy.market.github.service;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductModuleContent;
import org.kohsuke.github.GHContent;

import java.util.List;

public interface GHAxonIvyProductRepoService {

  GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);

  void extractReadMeFileFromContents(Product product, List<GHContent> contents,
      ProductModuleContent productModuleContent);
}
