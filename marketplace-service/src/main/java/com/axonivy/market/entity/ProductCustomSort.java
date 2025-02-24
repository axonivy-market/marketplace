package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_CUSTOM_SORT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = PRODUCT_CUSTOM_SORT)
public class ProductCustomSort {
  @Id
  private String ruleForRemainder;
}
