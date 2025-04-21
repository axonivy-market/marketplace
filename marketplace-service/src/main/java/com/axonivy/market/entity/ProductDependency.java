package com.axonivy.market.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "used_by_dependency_id")
  private List<ProductDependency> dependencies;

}
