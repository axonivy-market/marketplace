package com.axonivy.market.repository.impl;

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
    return Aggregation.match(Criteria.where("_id").is(id));
  }

  private AggregationOperation createReturnFirstModuleContentOperation() {
    return context -> new Document("$project",
        new Document("productModuleContents",
            new Document("$arrayElemAt", Arrays.asList("$productModuleContents", 0))
        )
    );
  }

  private AggregationOperation createReturnFirstMatchTagModuleContentOperation(String tag) {
    return context -> new Document("$project",
        new Document("productModuleContents",
            new Document("$filter",
                new Document("input", "$productModuleContents")
                    .append("as", "productModuleContent")
                    .append("cond", new Document("$eq", Arrays.asList("$$productModuleContent.tag", tag))))
        )
    );
  }

  public Product queryProductByAggregation(Aggregation aggregation) {
    return mongoTemplate.aggregate(aggregation, "Product", Product.class).getUniqueMappedResult();
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
    AggregationOperation returnReleasedVersionsAggregation = context -> new Document("$project",
        new Document("name", 1)
    );
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id), returnReleasedVersionsAggregation);
    Product product = queryProductByAggregation(aggregation);
    if (Objects.isNull(product)) {
      return Collections.emptyList();
    } else {
      return product.getReleasedVersions();
    }
  }
}
