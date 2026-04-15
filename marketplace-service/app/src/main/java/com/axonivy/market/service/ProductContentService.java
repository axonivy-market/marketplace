package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.ProductModuleContent;

import java.io.OutputStream;
import java.util.List;

public interface ProductContentService {

  /**
   * <p>
   * Extracts and retrieves README content and product documentation from a specific product version.
   * Downloads the artifact, extracts documentation files, parses README markdown, and returns all
   * product-related content for display in the marketplace UI.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  version
   *              type {@link String} - the specific product version to extract content from
   * @param  url
   *              type {@link String} - the download URL for the product artifact
   * @param  artifact
   *              type {@link Artifact} - the artifact entity containing Maven coordinates and metadata
   * @param  productName
   *              type {@link String} - the product name for context and logging
   * @return {@link ProductModuleContent} - parsed product content including README, features, and documentation;
   *         returns null if content cannot be extracted or parsed
   * @author nntthuy
   */
  ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName);

  /**
   * <p>
   * Retrieves the download URLs for all dependency artifacts of a specific product artifact version.
   * Parses the artifact's POM file and generates download URLs for each Maven dependency, allowing
   * bulk download or dependency analysis.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  artifactId
   *              type {@link String} - the specific artifact within the product
   * @param  version
   *              type {@link String} - the version of the artifact to analyze dependencies for
   * @return {@link List<String>} - list of download URLs for all dependencies; returns empty list if
   *         artifact not found or has no dependencies
   * @author ntqdinh
   */
  List<String> getDependencyUrls(String productId, String artifactId, String version);

  /**
   * <p>
   * Downloads multiple artifacts from provided URLs and builds a ZIP stream containing all of them.
   * Used for bulk downloading product and its dependencies in a single compressed file. Writes the
   * compressed ZIP data to the provided output stream suitable for HTTP response transmission.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the product ID for logging and context
   * @param  urls
   *              type {@link List<String>} - list of artifact download URLs to fetch and include
   * @param  out
   *              type {@link OutputStream} - the output stream to write the ZIP file data to
   * @return void - no return value; ZIP data is written directly to the output stream
   * @author ntqdinh
   */
  void buildArtifactZipStreamFromUrls(String productId, List<String> urls, OutputStream out);
}
