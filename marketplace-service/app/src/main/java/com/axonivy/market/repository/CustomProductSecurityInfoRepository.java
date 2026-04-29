package com.axonivy.market.repository;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProductSecurityInfoRepository {
  Page<ProductSecurityInfo> searchProductSecurityAndSorting(ProductSecurityCriteria criteria, Pageable pageable);
}
