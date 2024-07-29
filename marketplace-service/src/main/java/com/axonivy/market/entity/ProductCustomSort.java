package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_CUSTOM_SORT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(PRODUCT_CUSTOM_SORT)
public class ProductCustomSort {
  private String ruleForRemainder;
}
