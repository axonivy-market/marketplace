package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.repository.CustomProductDependencyRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.MongoDBConstants.*;

@AllArgsConstructor
@Repository
public class CustomProductDependencyRepositoryImpl implements CustomProductDependencyRepository {
  private static final String DEPENDENCIES_OF_ARTIFACT = "dependenciesOfArtifact";
  private static final String PRODUCT_ARTIFACTS_BY_VERSION = "productArtifactsByVersion";
  private final MongoTemplate mongoTemplate;

  @Override
  public List<ProductDependency> findProductDependencies(String productId, String artifactId, String version) {
    String versionKey = DEPENDENCIES_OF_ARTIFACT + DOT_SEPARATOR + artifactId;
    var query = new Query();
    query.addCriteria(Criteria.where(ID).is(productId).and(versionKey + DOT_SEPARATOR + VERSION).is(version));
    includeFirstMatchFilter(query, versionKey);
    return mongoTemplate.find(query, ProductDependency.class, EntityConstants.PRODUCT_DEPENDENCY);
  }

  private void includeFirstMatchFilter(Query query, String field) {
    query.fields().include(field + FIRST_MATCH_REGEX);
  }

}
