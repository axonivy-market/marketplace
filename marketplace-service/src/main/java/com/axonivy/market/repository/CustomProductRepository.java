package com.axonivy.market.repository;

import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomProductRepository {
  Product getProductByIdAndVersion(String id, String version);

  Product getProductWithModuleContent(String id);

  Product findProductById(String id);

  List<String> getReleasedVersionsById(String id);

  List<Product> getAllProductsWithIdAndReleaseTagAndArtifact();

  Page<Product> searchByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findByCriteria(ProductSearchCriteria criteria);

  List<Product> findAllProductsHaveDocument();
}
