package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DEPENDENCY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT_DEPENDENCY)
public class ProductDependency extends AuditableIdEntity {
  private String productId;
  private String artifactId;
  private String version;
  private String downloadUrl;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<ProductDependency> dependencies;

}
