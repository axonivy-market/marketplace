package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.CustomProductModuleContentRepository;
import com.axonivy.market.repository.CustomRepository;
import lombok.Builder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;

@Builder
public class CustomProductModuleContentRepositoryImpl extends CustomRepository implements CustomProductModuleContentRepository {

  private final MongoTemplate mongoTemplate;

  public CustomProductModuleContentRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<String> findVersionsByProductId(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createFieldMatchOperation(MongoDBConstants.PRODUCT_ID, id),
        createProjectAggregationBySingleFieldName(MongoDBConstants.VERSION));
    return queryProductModuleContentsByAggregation(aggregation).stream().map(ProductModuleContent::getVersion).toList();
  }

  public List<ProductModuleContent> queryProductModuleContentsByAggregation(Aggregation aggregation) {
    return mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT_MODULE_CONTENT, ProductModuleContent.class)
        .getMappedResults();
  }
}