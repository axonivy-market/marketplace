package com.axonivy.market.criteria;

import com.axonivy.market.enums.ProductSecuritySortOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSecurityCriteria {
  String searchText;
  ProductSecuritySortOption sortOption;
  String sortDirection;
}
