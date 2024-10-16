package com.axonivy.market.repository;

import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomProductRepository {
  Product getProductByIdWithTagOrVersion(String id, String tag);

  Product getProductById(String id);

  List<String> getReleasedVersionsById(String id);

  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);

  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);

  List<Product> getAllProductsWithIdAndReleaseTagAndArtifact();

  ProductModuleContent findByProductIdAndTagOrMavenVersion(String productId, String tag);

  Page<Product> searchByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findByCriteria(ProductSearchCriteria criteria);

  List<Product> findAllProductsHaveDocument();
}
