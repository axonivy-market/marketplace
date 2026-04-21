package com.axonivy.market.criteria;

import com.axonivy.market.enums.ProductSecuritySortOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductSecurityCriteria {
  String searchText;
  ProductSecuritySortOption sortOption;
  String sortDirection;
}
