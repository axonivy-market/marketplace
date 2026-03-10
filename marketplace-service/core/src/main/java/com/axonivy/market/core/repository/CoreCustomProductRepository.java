package com.axonivy.market.core.repository;

import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CoreCustomProductRepository {
  Page<Product> searchByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findByCriteria(ProductSearchCriteria criteria);

  Product getProductByIdAndVersion(String id, String version);

  Product findProductByIdAndRelatedData(String id);

  List<String> getReleasedVersionsById(String id);
}
