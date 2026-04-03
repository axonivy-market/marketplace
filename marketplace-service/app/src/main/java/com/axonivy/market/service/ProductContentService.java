package com.axonivy.market.service;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.ProductModuleContent;

import java.io.OutputStream;
import java.util.List;

public interface ProductContentService {

  /**
   * <p>
   * Get readme content and product content from version
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @param  url
   *              type {@link String}
   * @param  artifact
   *              type {@link Artifact}
   * @param  productName
   *              type {@link String}
   * @return {@link ProductModuleContent}
   * @author nntthuy
   */
  ProductModuleContent getReadmeAndProductContentsFromVersion(String productId, String version, String url,
      Artifact artifact, String productName);

  /**
   * <p>
   * Get dependency urls
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  artifactId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @return {@link List<String>}
   * @author ntqdinh
   */
  List<String> getDependencyUrls(String productId, String artifactId, String version);

  /**
   * <p>
   * Build artifact zip stream from urls
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  urls
   *              type {@link List<String>}
   * @param  out
   *              type {@link OutputStream}
   * @return {@link }
   * @author ntqdinh
   */
  void buildArtifactZipStreamFromUrls(String productId, List<String> urls, OutputStream out);
}
