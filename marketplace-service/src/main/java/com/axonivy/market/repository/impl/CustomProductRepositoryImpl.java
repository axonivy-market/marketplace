package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.CustomProductRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class CustomProductRepositoryImpl implements CustomProductRepository {
  private final MongoTemplate mongoTemplate;

  public CustomProductRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  private AggregationOperation createIdMatchOperation(String id) {
    return Aggregation.match(Criteria.where(MongoDBConstants.ID).is(id));
  }

  private AggregationOperation createReturnFirstModuleContentOperation() {
    return context -> new Document(MongoDBConstants.ADD_FIELD, new Document(MongoDBConstants.PRODUCT_MODULE_CONTENTS, new Document(MongoDBConstants.FILTER, new Document(MongoDBConstants.INPUT, MongoDBConstants.PRODUCT_MODULE_CONTENT_QUERY).append(MongoDBConstants.AS, MongoDBConstants.PRODUCT_MODULE_CONTENT).append(MongoDBConstants.CONDITION, new Document(MongoDBConstants.EQUAL, Arrays.asList(MongoDBConstants.PRODUCT_MODULE_CONTENT_TAG, MongoDBConstants.NEWEST_RELEASED_VERSION_QUERY))))));
  }

  private AggregationOperation createReturnFirstMatchTagModuleContentOperation(String tag) {
    return context -> new Document(MongoDBConstants.ADD_FIELD, new Document(MongoDBConstants.PRODUCT_MODULE_CONTENTS, new Document(MongoDBConstants.FILTER, new Document(MongoDBConstants.INPUT, MongoDBConstants.PRODUCT_MODULE_CONTENT_QUERY).append(MongoDBConstants.AS, MongoDBConstants.PRODUCT_MODULE_CONTENT).append(MongoDBConstants.CONDITION, new Document(MongoDBConstants.EQUAL, Arrays.asList(MongoDBConstants.PRODUCT_MODULE_CONTENT_TAG, tag))))));
  }

  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, MongoDBConstants.PRODUCT_COLLECTION, Product.class)).map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  @Override
  public Product getProductByIdAndTag(String id, String tag) {
    // Create the aggregation pipeline
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id), createReturnFirstMatchTagModuleContentOperation(tag));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public Product getProductById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id), createReturnFirstModuleContentOperation());
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

  public void updateInitialCount(String productId, int initialCount) {
    Update update = new Update().inc("InstallationCount", initialCount).set("SynchronizedInstallationCount", true);
    mongoTemplate.updateFirst(createQueryById(productId), update, Product.class);
  }

  @Override
  public int increaseInstallationCount(String productId) {
    Update update = new Update().inc("InstallationCount", 1);
    // Find and modify the document, then return the updated InstallationCount field
    Product updatedProduct = mongoTemplate.findAndModify(createQueryById(productId), update, FindAndModifyOptions.options().returnNew(true), Product.class);
    return updatedProduct != null ? updatedProduct.getInstallationCount() : 0;
  }

  private Query createQueryById(String id) {
    return new Query(Criteria.where(MongoDBConstants.ID).is(id));
  }
}