package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  List<String> syncLatestDataFromMarketRepo();

  Product fetchProductDetail(String id, Boolean isShowDevVersion);

  String getCompatibilityFromOldestTag(String oldestTag);

  void clearAllProducts();

  Product fetchBestMatchProductDetail(String id, String version);

  Product fetchProductDetailByIdAndVersion(String id, String version);

  boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath);
}
