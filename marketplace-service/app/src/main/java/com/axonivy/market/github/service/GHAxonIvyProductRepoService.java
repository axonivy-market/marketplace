package com.axonivy.market.github.service;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductModuleContent;
import org.kohsuke.github.GHContent;

import java.util.List;

public interface GHAxonIvyProductRepoService {

  /**
   * <p>
   * Retrieves a specific file from a GitHub repository at a given tag version.
   * Fetches file content from a product repository at a specific release tag.
   * </p>
   *
   * @param  repoName
   *              type {@link String} - the GitHub repository name (format: "owner/repo")
   * @param  filePath
   *              type {@link String} - the path to the file within the repository
   * @param  tagVersion
   *              type {@link String} - the Git tag or version to fetch the file from
   * @return {@link GHContent} - the file content with metadata; returns null if file not found
   * @author ntqdinh
   */
  GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion);

  /**
   * <p>
   * Extracts and parses README files from GitHub repository contents. Reads markdown README files,
   * processes their content, and populates the product module content with formatted documentation.
   * </p>
   *
   * @param  product
   *              type {@link Product} - the product entity being processed
   * @param  contents
   *              type {@link List<GHContent>} - list of files from the GitHub repository
   * @param  productModuleContent
   *              type {@link ProductModuleContent} - the product content object to populate with README data
   * @return void - README data is extracted and stored in the productModuleContent object
   * @author ntqdinh
   */
  void extractReadMeFileFromContents(Product product, List<GHContent> contents,
      ProductModuleContent productModuleContent);
}
