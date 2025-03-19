package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
import static com.axonivy.market.constants.EntityConstants.PRODUCT_ID_FK;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT_DEPENDENCY)
public class ProductDependency extends AuditableEntity {
  @Id
  private String productId;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = PRODUCT_ID_FK)
  private List<MavenDependency> dependenciesOfArtifact;

  @Override
  public String getId() {
    return productId;
  }

  @Override
  public void setId(String productId) {
    this.productId = productId;
  }
}
