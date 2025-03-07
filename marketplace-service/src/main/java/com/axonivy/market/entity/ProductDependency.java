package com.axonivy.market.entity;

import com.axonivy.market.bo.MavenDependency;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.List;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DEPENDENCY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT_DEPENDENCY)
public class ProductDependency {
  @Id
  private String productId;

  @CreatedDate
  private Date createdAt;
  @LastModifiedDate
  private Date modifiedAt;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id_fk")
  private List<MavenDependency> dependenciesOfArtifact;
}
