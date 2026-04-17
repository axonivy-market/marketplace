package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.SecurityMonitorSortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProductSecurityInfoRepository {
  Page<ProductSecurityInfo> searchProductSecurityAndSorting(String searchText, SecurityMonitorSortOption sortOption,
      Pageable pageable);
}
