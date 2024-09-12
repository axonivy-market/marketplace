package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.MongoDBConstants;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class CustomRepository {
  protected AggregationOperation createIdMatchOperation(String id) {
    return createFieldMatchOperation(MongoDBConstants.ID, id);
  }

  protected Query createQueryById(String id) {
    return new Query(Criteria.where(MongoDBConstants.ID).is(id));
  }

  AggregationOperation createProjectAggregationBySingleFieldName(String fieldName) {
    return Aggregation.project(MongoDBConstants.TAG);
  }

  protected AggregationOperation createFieldMatchOperation(String fieldName, String id) {
    return Aggregation.match(Criteria.where(fieldName).is(id));
  }
}