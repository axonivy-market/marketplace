package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Objects;
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
  @Serial
  private static final long serialVersionUID = 1;

  private String productId;
  private String artifactId;
  private String version;
  private String downloadUrl;

  @ManyToMany(fetch = FetchType.LAZY)
  private Set<ProductDependency> dependencies;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProductDependency dependency)) return false;
    return Objects.equals(productId, dependency.productId) && Objects.equals(artifactId,
        dependency.artifactId) && Objects.equals(version, dependency.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId, artifactId, version);
  }
}
