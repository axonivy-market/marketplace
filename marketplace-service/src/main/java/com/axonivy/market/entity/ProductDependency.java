package com.axonivy.market.entity;

import com.axonivy.market.bo.MavenDependency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DEPENDENCY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(PRODUCT_DEPENDENCY)
public class ProductDependency {
  @Id
  private String productId;
  private Map<String, List<MavenDependency>> dependenciesOfArtifact;
  @CreatedDate
  private Date createdAt;
  @LastModifiedDate
  private Date modifiedAt;
}
