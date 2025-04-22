package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DEPENDENCY;
import static com.axonivy.market.constants.EntityConstants.USED_BY_DEPENDENCY_ID;

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

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = USED_BY_DEPENDENCY_ID)
  private List<ProductDependency> dependencies;

}
