package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.CustomProductRepository;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Log4j2
public class CustomProductRepositoryImpl implements CustomProductRepository {
  private final MongoTemplate mongoTemplate;

  public CustomProductRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private AggregationOperation createIdMatchOperation(String id) {
    return Aggregation.match(Criteria.where(MongoDBConstants.ID).is(id));
  }

  private AggregationOperation createReturnFirstMatchTagModuleContentOperation(String tag) {
    return context -> new Document(MongoDBConstants.PROJECT,
        new Document(MongoDBConstants.PRODUCT_MODULE_CONTENT,
            new Document(MongoDBConstants.FILTER,
                new Document(MongoDBConstants.INPUT, MongoDBConstants.PRODUCT_MODULE_CONTENT_QUERY)
                    .append(MongoDBConstants.AS, MongoDBConstants.PRODUCT_MODULE_CONTENT)
                    .append(MongoDBConstants.CONDITION, new Document(MongoDBConstants.EQUAL, Arrays.asList(MongoDBConstants.PRODUCT_MODULE_CONTENT_TAG, tag))))
        )
    );
  }

  public Product queryProductByAggregation(Aggregation aggregation) {
    return mongoTemplate.aggregate(aggregation, MongoDBConstants.PRODUCT_COLLECTION, Product.class).getUniqueMappedResult();
  }

  @Override
  public Product getProductByIdAndTag(String id, String tag) {
    // Create the aggregation pipeline
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id), createReturnFirstMatchTagModuleContentOperation(tag));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public Product getProductById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    Product product = queryProductByAggregation(aggregation);
    if (Objects.isNull(product)) {
      return Collections.emptyList();
    } else {
      return product.getReleasedVersions();
    }
  }
}
