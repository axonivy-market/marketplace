package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.OutputStream;

public interface ProductMarketplaceDataService {

  /**
   * <p>
   * Add custom sort for product
   * </p>
   *
   * @param  customSort
   *              type {@link ProductCustomSortRequest}
   * @return {@link }
   * @author nntthuy
   */
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  /**
   * <p>
   * Get custom sort products
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link ProductCustomSortRequest}
   * @author nntthuy
   */
  ProductCustomSortRequest getCustomSortProducts();

  /**
   * <p>
   * Update installation count for product
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @param  designerVersion
   *              type {@link String}
   * @return {@link int}
   * @author nntthuy
   */
  int updateInstallationCountForProduct(String id, String designerVersion);

  /**
   * <p>
   * Update product installation count
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link }
   * @author thxhuy
   */
  int updateProductInstallationCount(String id);

  /**
   * <p>
   * Get product marketplace data
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link ProductMarketplaceData}
   * @author nntthuy
   */
  ProductMarketplaceData getProductMarketplaceData(String id);

  /**
   * <p>
   * Get installation count
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link Integer}
   * @author nntthuy
   */
  Integer getInstallationCount(String id);

  /**
   * <p>
   * Get product artifact stream
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  artifactId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @return {@link ResponseEntity<Resource>}
   * @author ntqdinh
   */
  ResponseEntity<Resource> getProductArtifactStream(String productId, String artifactId, String version);

  /**
   * <p>
   * Build artifact stream from resource
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  resource
   *              type {@link Resource}
   * @param  outputStream
   *              type {@link OutputStream}
   * @return {@link OutputStream}
   * @author ntqdinh
   */
  OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream);
}
