package com.axonivy.market.service;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.model.GitHubReleaseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface ProductService {

  /**
   * <p>
   * Find products
   * </p>
   *
   * @param  type
   *              type {@link String}
   * @param  keyword
   *              type {@link String}
   * @param  language
   *              type {@link String}
   * @param  isRESTClient
   *              type {@link Boolean}
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<Product>}
   * @author nntthuy
   */
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  /**
   * <p>
   * Synchronize latest data from market repositories
   * </p>
   *
   * @param  resetSync
   *              type {@link Boolean}
   * @return {@link List<String>}
   * @author nntthuy
   */
  List<String> syncLatestDataFromMarketRepo(Boolean resetSync);

  /**
   * <p>
   * fetchProductDetail
   * </p>
   *
   * @param  
   *              type {@link }
   * @return {@link }
   * @author thxhuy
   */
  Product fetchProductDetail(String id, Boolean isShowDevVersion);

  /**
   * <p>
   * Fetch best match product detail
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @return {@link Product}
   * @author ntqdinh
   */
  Product fetchBestMatchProductDetail(String id, String version);

  /**
   * <p>
   * Fetch product detail by id and version
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @return {@link Product}
   * @author ntqdinh
   */
  Product fetchProductDetailByIdAndVersion(String id, String version);

  /**
   * <p>
   * Synchronize data for one product
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  marketItemPath
   *              type {@link String}
   * @param  overrideMarketItemPath
   *              type {@link Boolean}
   * @return {@link boolean}
   * @author phhung
   */
  boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath);

  /**
   * <p>
   * Synchronize first published date of all products
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link boolean}
   * @author phhung
   */
  boolean syncFirstPublishedDateOfAllProducts();

  /**
   * <p>
   * Get GitHub release models
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<GitHubReleaseModel>}
   * @author vhhoang
   */
  Page<GitHubReleaseModel> getGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  /**
   * <p>
   * Synchronize GitHub release models
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<GitHubReleaseModel>}
   * @author vhhoang
   */
  Page<GitHubReleaseModel> syncGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  /**
   * <p>
   * Get GitHub release model by product id and release id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  releaseId
   *              type {@link Long}
   * @return {@link GitHubReleaseModel}
   * @author vhhoang
   */
  GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(String productId, Long releaseId) throws IOException;

  /**
   * <p>
   * Get product ids
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link List<String>}
   * @author nntthuy
   */
  List<String> getProductIds();

  /**
   * <p>
   * Renew product by id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  marketItemPath
   *              type {@link String}
   * @param  overrideMarketItemPath
   *              type {@link Boolean}
   * @return {@link Product}
   * @author tvtphuc
   */
  Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath);

  /**
   * <p>
   * Get best match version of product
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @param  isShowDevVersion
   *              type {@link Boolean}
   * @return {@link }
   * @author pvquan
   */
  String getBestMatchVersion(String productId, String version, Boolean isShowDevVersion);
}
