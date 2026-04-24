package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductMarketplaceDataService {

  /**
   * <p>
   * Adds or updates custom sort order configuration for products in the marketplace. Allows administrators
   * to manually control the display order of products on the marketplace homepage and search results,
   * overriding default sorting algorithms.
   * </p>
   *
   * @param  customSort
   *              type {@link ProductCustomSortRequest} - request object containing list of product IDs
   *              with their desired sort order/positions
   * @return void - no return value; custom sort configuration is persisted to the database
   * @author nntthuy
   */
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  /**
   * <p>
   * Retrieves the current custom sort order configuration for all products. Returns the list of products
   * with their manually configured sort positions if custom sorting has been configured; otherwise returns default
   * configuration.
   * </p>
   *
   * @return {@link ProductCustomSortRequest} - object containing the current custom sort order for all products;
   *         returns null if no custom sort has been configured
   * @author nntthuy
   */
  ProductCustomSortRequest getCustomSortProducts();

  /**
   * <p>
   * Increments the installation count for a product for a specific AxonIvy Designer version. Tracks how
   * many times users have installed or downloaded a product through the Designer plugin installer for
   * the specified Designer version.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier
   * @param  designerVersion
   *              type {@link String} - the AxonIvy Designer version (e.g., "12.0", "13.0") to track installations for
   * @return {@link int} - the updated installation count for the product/version combination
   * @author nntthuy
   */
  int updateInstallationCountForProduct(String id, String designerVersion);

  /**
   * <p>
   * Initializes or synchronizes the total installation count for a product from legacy data. If the product
   * hasn't been synchronized yet, reads installation counts from a legacy file or generates a random initial
   * count, then updates the database with this initial value. Subsequent calls return the existing data
   * without modification.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier to initialize installation count for
   * @return {@link ProductMarketplaceData} - the updated ProductMarketplaceData object with synchronized
   *         installation count; returns existing data if already synchronized
   * @author nntthuy
   */
  ProductMarketplaceData updateProductInstallationCount(String id);

  /**
   * <p>
   * Retrieves marketplace-specific metadata for a product. Returns installation statistics, view counts,
   * download information, and other marketplace analytics data used for trending and popularity rankings.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier
   * @return {@link ProductMarketplaceData} - marketplace statistics and analytics data for the product;
   *         returns a default empty ProductMarketplaceData if not found
   * @author nntthuy
   */
  ProductMarketplaceData getProductMarketplaceData(String id);

  /**
   * <p>
   * Retrieves the total installation count for a product. Returns the cumulative number of times the
   * product has been installed by users across all Designer versions.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier
   * @return {@link Integer} - the total number of installations; returns 0 if no installations recorded
   * @author nntthuy
   */
  Integer getInstallationCount(String id);

  /**
   * <p>
   * Retrieves a product artifact file as a downloadable resource stream. Returns the artifact file ready
   * for HTTP transmission with appropriate content-type headers and file metadata (size, filename, etc.).
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  artifactId
   *              type {@link String} - the specific artifact ID within the product
   * @param  version
   *              type {@link String} - the product version containing the artifact
   * @return {@link ResponseEntity<Resource>} - HTTP response entity with the artifact file resource and
   *         appropriate headers (Content-Disposition, Content-Type, Content-Length)
   * @author ntqdinh
   */
  ResponseEntity<Resource> getProductArtifactStream(String productId, String artifactId, String version);

  /**
   * <p>
   * Builds and writes artifact data from a resource to an output stream. Used internally to stream artifact
   * file content to HTTP response streams or file writers. Handles buffering and efficient data transfer.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the product ID for logging and context purposes
   * @param  resource
   *              type {@link Resource} - the Spring resource object representing the artifact file
   * @param  outputStream
   *              type {@link OutputStream} - the output stream to write artifact data to
   * @return {@link OutputStream} - the same output stream after writing (for chaining); may be closed
   *         depending on implementation
   * @author ntqdinh
   */
  OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream);

  /**
   * <p>
   * Updates successor information and deprecation status for a product. Sets successor URL, deprecation
   * requester, and deprecation date in the marketplace data. Updates the product's deprecated status and
   * optionally creates a GitHub pull request to update the product's README with successor notes.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to update
   * @param  deprecationRequest
   *              type {@link DeprecationRequest} - request containing successor URL, deprecation details,
   *              and pull request configuration
   * @return {@link String} - the GitHub pull request URL if a pull request was created; returns null
   *         if no pull request was created or product not found
   * @throws IOException if an error occurs during GitHub API operations or file processing
   * @author tvtphuc
   */
  String updateSuccessorForProduct(String productId, DeprecationRequest deprecationRequest) throws IOException;

  /**
   * <p>
   * Retrieves product IDs filtered by their deprecation status. Returns a list of product projections
   * containing product IDs and deprecation information for products that match the specified deprecated status.
   * </p>
   *
   * @param  isDeprecated
   *              type {@link Boolean} - filter flag; true for deprecated products, false for active products,
   *              null to retrieve all products
   * @return {@link List<ProductDeprecationProjection>} - list of product deprecation projections matching
   *         the specified deprecation status; returns empty list if no products match the criteria
   * @author tvtphuc
   */
  List<ProductDeprecationProjection> getProductIdsByDeprecated(Boolean isDeprecated);
}
