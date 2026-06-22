package com.axonivy.market.service;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.model.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface ProductService {

  /**
   * <p>
   * Searches and filters products with advanced criteria including type, keyword search, language,
   * and REST client compatibility. Returns paginated results for efficient large dataset handling.
   * </p>
   *
   * @param type         type {@link String} - product type filter (e.g., "Connector", "Plugin"); can be null for no
   *                     type filtering
   * @param keyword      type {@link String} - search keyword to filter products by name or description; can be null
   *                     for no keyword filtering
   * @param language     type {@link String} - language code filter for product documentation (e.g., "en", "de"); can
   *                    be null
   * @param isRESTClient type {@link Boolean} - if true, filters for products compatible with REST client; if false,
   *                     excludes REST client products; can be null
   * @param pageable     type {@link Pageable} - pagination and sorting configuration (page number, page size, sort
   *                     order)
   * @return {@link Page<Product>} - paginated list of products matching all criteria with total count and page
   * information
   * @author nntthuy
   */
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  /**
   * <p>
   * Synchronizes the latest product data from market repositories. Downloads and updates product information,
   * versions, artifacts, and metadata from configured GitHub repositories. Optionally performs a full reset
   * to clear existing data before synchronization.
   * </p>
   *
   * @param  resetSync
   *              type {@link Boolean} - if true, clears all existing product data before synchronization;
   *              if false, performs incremental update of changed products only
   * @return {@link List<String>} - list of product IDs that were synchronized; returns empty list if no products
   *         were found or synchronization failed
   * @author nntthuy
   */
  List<String> syncLatestDataFromMarketRepo(Boolean resetSync);

  /**
   * <p>
   * Retrieves detailed information for a specific product including versions, artifacts, metadata,
   * and images. Optionally includes development versions (dev, nightly) based on the isShowDevVersion parameter.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier to fetch details for
   * @param  isShowDevVersion
   *              type {@link Boolean} - if true, includes development versions (dev, nightly) in the result;
   *              if false, only shows released versions; null defaults to false
   * @return {@link Product} - complete product object with all related entities (versions, artifacts, metadata, images);
   *         returns exception if product not found
   * @author thxhuy
   */
  Product fetchProductDetail(String id, Boolean isShowDevVersion);

  /**
   * <p>
   * Retrieves the best matching product detail for a specific version. If the exact version is not available,
   * automatically resolves to the closest compatible version using semantic versioning logic.
   * </p>
   *
   * @param id      type {@link String} - the unique product identifier to fetch details for
   * @param version type {@link String} - the requested product version; if exact match not found, best available
   *                version is automatically resolved
   * @return {@link Product} - complete product object with details for the resolved version; returns exception
   * if product not found or no suitable version available
   * @author ntqdinh
   */
  Product fetchBestMatchProductDetail(String id, String version);

  /**
   * <p>
   * Retrieves product details for a specific ID and exact version combination. Returns complete product
   * information only if the exact specified version exists and is available.
   * </p>
   *
   * @param id      type {@link String} - the unique product identifier to fetch details for
   * @param version type {@link String} - the exact product version to retrieve data for; must match released version
   *               exactly
   * @return {@link Product} - complete product object with details for the specified version; returns null
   * if product not found or specified version does not exist
   * @author ntqdinh
   */
  Product fetchProductDetailByIdAndVersion(String id, String version);

  /**
   * <p>
   * Synchronizes data for a single product from its source repository. Updates product metadata, versions,
   * artifacts, and other information. Optionally overrides the market item path for custom synchronization.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to synchronize
   * @param  marketItemPath
   *              type {@link String} - the market item path to use for synchronization; can be null to use default
   * @param  overrideMarketItemPath
   *              type {@link Boolean} - if true, forces the use of the provided marketItemPath even if different
   *              from the stored path; if false, uses the stored path
   * @return {@link boolean} - true if synchronization was successful; false if synchronization failed
   * @author phhung
   */
  boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath);

  /**
   * <p>
   * Synchronizes the first published date for all products in the system. Updates the publication timestamp
   * for each product based on the earliest release date found in the source repositories.
   * </p>
   *
   * @return {@link boolean} - true if synchronization was successful; false if synchronization failed
   * @author phhung
   */
  boolean syncFirstPublishedDateOfAllProducts();

  /**
   * <p>
   * Retrieves GitHub release models for a specific product with pagination. Returns formatted release
   * information including assets, notes, and metadata from the product's GitHub repository.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to retrieve releases for
   * @param  pageable
   *              type {@link Pageable} - pagination configuration for the results
   * @return {@link Page<GitHubReleaseModel>} - paginated list of GitHub release models with metadata
   * @throws IOException - if GitHub API call fails or product repository not found
   * @author vhhoang
   */
  Page<GitHubReleaseModel> getGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  /**
   * <p>
   * Synchronizes and retrieves GitHub release models for a specific product. Downloads the latest release
   * information from GitHub and caches it locally, then returns the paginated results.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to synchronize releases for
   * @param  pageable
   *              type {@link Pageable} - pagination configuration for the results
   * @return {@link Page<GitHubReleaseModel>} - paginated list of synchronized GitHub release models
   * @throws IOException - if GitHub API call fails or synchronization error occurs
   * @author vhhoang
   */
  Page<GitHubReleaseModel> syncGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  /**
   * <p>
   * Retrieves a specific GitHub release model by product ID and release ID. Returns complete release
   * information including assets, notes, and metadata for a single release.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  releaseId
   *              type {@link Long} - the GitHub release ID to retrieve
   * @return {@link GitHubReleaseModel} - the GitHub release model with complete metadata; returns null
   *         if release not found
   * @throws IOException - if GitHub API call fails or release not found
   * @author vhhoang
   */
  GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(String productId, Long releaseId) throws IOException;

  /**
   * <p>
   * Retrieves all product IDs currently available in the marketplace. Returns a complete list of
   * unique identifiers for all products that have been synchronized and are available for installation.
   * </p>
   *
   * @return {@link List<String>} - list of all product IDs in the system; returns empty list if no products
   *         are available
   * @author nntthuy
   */
  List<String> getProductIds();

  /**
   * <p>
   * Refreshes and updates a product by its ID, optionally overriding the market item path. Forces a
   * complete resynchronization of product data from the source repository, updating metadata, versions,
   * artifacts, and other product information.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to refresh
   * @param  marketItemPath
   *              type {@link String} - the market item path to use for synchronization; can be null to use default
   * @param  overrideMarketItemPath
   *              type {@link Boolean} - if true, forces the use of the provided marketItemPath even if different
   *              from the stored path; if false, uses the stored path
   * @return {@link Product} - the refreshed product object with updated data; returns null if product not found
   * @author tvtphuc
   */
  Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath);

  /**
   * <p>
   * Resolves the best matching version for a product using semantic versioning logic. If the exact
   * requested version is not available, returns the closest compatible version. Optionally includes
   * development versions (dev, nightly) in the resolution process.
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier
   * @param  version
   *              type {@link String} - the requested product version; if exact match not found, best available
   *              version is automatically resolved
   * @param  isShowDevVersion
   *              type {@link Boolean} - if true, includes development versions in version resolution;
   *              if false, only considers released versions; null defaults to false
   * @return {@link String} - the resolved version string; returns null if product not found or no suitable
   *         version available
   * @author pvquan
   */
  String getBestMatchVersion(String productId, String version, Boolean isShowDevVersion);

  /**
   * <p>
   * Updates a product's information based on the provided update request. Modifies product metadata,
   * versions, artifacts, and other details according to the fields specified in the UpdateProductRequest.
   * </p>
   *
   * @param  id
   *              type {@link String} - the unique product identifier to update
   * @param  request
   *              type {@link UpdateProductRequest} - the request object containing updated product information
   * @return {@link Product} - the updated product object with new data; throws exception if product not found.
   * @author pvquan
   */
  Product updateProduct(String id, UpdateProductRequest request);
}
