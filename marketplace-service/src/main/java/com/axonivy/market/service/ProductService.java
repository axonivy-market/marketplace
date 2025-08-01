package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.GitHubReleaseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  List<String> syncLatestDataFromMarketRepo(Boolean resetSync);

  Product fetchProductDetail(String id, Boolean isShowDevVersion);

  Product fetchBestMatchProductDetail(String id, String version);

  Product fetchProductDetailByIdAndVersion(String id, String version);

  boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath);

  boolean syncFirstPublishedDateOfAllProducts();

  Page<GitHubReleaseModel> getGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  Page<GitHubReleaseModel> syncGitHubReleaseModels(String productId, Pageable pageable) throws IOException;

  GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(String productId, Long releaseId) throws IOException;

  List<String> getProductIdList();

  Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath);
}
