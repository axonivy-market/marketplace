package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.CustomRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import lombok.Builder;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Builder
public class CustomProductRepositoryImpl extends CustomRepository implements CustomProductRepository {
  private final MongoTemplate mongoTemplate;
  private final ProductModuleContentRepository contentRepository;

  public CustomProductRepositoryImpl(MongoTemplate mongoTemplate, ProductModuleContentRepository contentRepository) {
    this.mongoTemplate = mongoTemplate;
    this.contentRepository = contentRepository;
  }


  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  public List<Product> queryProductsByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getMappedResults).orElse(Collections.emptyList());
  }

  @Override
  public Product getProductByIdWithTagOrVersion(String id, String tag) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = findByProductIdAndTagOrMavenVersion(id, tag);
      result.setProductModuleContent(content);
    }
    return result;
  }

  @Override
  public ProductModuleContent findByProductIdAndTagOrMavenVersion(String productId, String tag) {
    Criteria productIdCriteria = Criteria.where(MongoDBConstants.PRODUCT_ID).is(productId);
    Criteria orCriteria = new Criteria().orOperator(
        Criteria.where(MongoDBConstants.TAG).is(tag),
        Criteria.where(MongoDBConstants.MAVEN_VERSIONS).in(tag)
    );
    Query query = new Query(new Criteria().andOperator(productIdCriteria, orCriteria));
    return mongoTemplate.findOne(query, ProductModuleContent.class);
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
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount).set(
        MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true);
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


  @Override
  public void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    mongoTemplate.upsert(createQueryByProductIdAndDesignerVersion(productId, designerVersion),
        update, ProductDesignerInstallation.class);
  }

  @Override
  public List<Product> getAllProductsWithIdAndReleaseTagAndArtifact() {
    return queryProductsByAggregation(
        createProjectIdAndReleasedVersionsAndArtifactsAggregation());

  }

  private Query createQueryByProductIdAndDesignerVersion(String productId, String designerVersion) {
    return new Query(Criteria.where(MongoDBConstants.PRODUCT_ID).is(productId)
        .andOperator(Criteria.where(MongoDBConstants.DESIGNER_VERSION).is(designerVersion)));
  }

  protected Aggregation createProjectIdAndReleasedVersionsAndArtifactsAggregation() {
    return Aggregation.newAggregation(
        Aggregation.project(MongoDBConstants.ID, MongoDBConstants.ARTIFACTS, MongoDBConstants.RELEASED_VERSIONS)
    );
  }
}
