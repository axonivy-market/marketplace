package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Builder
public class CustomProductRepositoryImpl implements CustomProductRepository {
  private final MongoTemplate mongoTemplate;
  private final ProductModuleContentRepository contentRepository;

  public CustomProductRepositoryImpl(MongoTemplate mongoTemplate, ProductModuleContentRepository contentRepository) {
    this.mongoTemplate = mongoTemplate;
      this.contentRepository = contentRepository;
  }

  private AggregationOperation createIdMatchOperation(String id) {
    return Aggregation.match(Criteria.where(MongoDBConstants.ID).is(id));
  }

  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, MongoDBConstants.PRODUCT_COLLECTION, Product.class))
        .map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  @Override
  public Product getProductByIdAndTag(String id, String tag) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByTagAndProductId(tag,id);
      result.setProductModuleContent(content);
    }
    return result;
  }

  private Product findProductById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public Product getProductById(String id) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByTagAndProductId(
          result.getNewestReleaseVersion(), id);
      result.setProductModuleContent(content);
    }
    return result;
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    Product product = queryProductByAggregation(aggregation);
    if (Objects.isNull(product)) {
      return Collections.emptyList();
    }
    return product.getReleasedVersions();
  }

  public int updateInitialCount(String productId, int initialCount) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount).set(MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true);
    mongoTemplate.updateFirst(createQueryById(productId), update, Product.class);
    return Optional.ofNullable(getProductById(productId)).map(Product::getInstallationCount).orElse(0);
  }

  @Override
  public int increaseInstallationCount(String productId) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    Product updatedProduct = mongoTemplate.findAndModify(createQueryById(productId), update,
        FindAndModifyOptions.options().returnNew(true), Product.class);
    return updatedProduct != null ? updatedProduct.getInstallationCount() : 0;
  }

  private Query createQueryById(String id) {
    return new Query(Criteria.where(MongoDBConstants.ID).is(id));
  }
}
