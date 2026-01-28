package com.axonivy.market.repository;

import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.repository.CoreCustomProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomProductRepository extends CoreCustomProductRepository {
  Product getProductByIdAndVersion(String id, String version);

  Product findProductByIdAndRelatedData(String id);

  List<String> getReleasedVersionsById(String id);

  List<Product> findAllProductsHaveDocument();
}
