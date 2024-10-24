package com.axonivy.market.github.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GHAxonIvyProductRepoService {

  GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);

  List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException;

  ProductModuleContent getReadmeAndProductContentsFromTag(Product product, GHRepository ghRepository, String tag);

  void extractReadMeFileFromContents(Product product, List<GHContent> contents,
      ProductModuleContent productModuleContent);

  void updateSetupPartForProductModuleContent(Product product,
      Map<String, Map<String, String>> moduleContents, String tag) throws IOException;
}
