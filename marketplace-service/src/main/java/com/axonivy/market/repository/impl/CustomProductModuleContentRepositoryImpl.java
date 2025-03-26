package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.BaseRepository;
import com.axonivy.market.repository.CustomProductModuleContentRepository;
import lombok.Builder;

import java.util.List;

import static com.axonivy.market.constants.PostgresDBConstants.PRODUCT_ID;
import static com.axonivy.market.constants.PostgresDBConstants.VERSION;

@Builder
public class CustomProductModuleContentRepositoryImpl extends BaseRepository<ProductModuleContent> implements CustomProductModuleContentRepository {

  @Override
  public List<String> findVersionsByProductId(String id) {
    CriteriaByTypeContext<ProductModuleContent, String> criteriaContext = createCriteriaTypeContext(String.class);
    criteriaContext.query().select(criteriaContext.root().get(VERSION))
        .where(criteriaContext.builder().equal(criteriaContext.root().get(PRODUCT_ID), id));
    return findByCriteria(criteriaContext);
  }

  @Override
  protected Class<ProductModuleContent> getType() {
    return ProductModuleContent.class;
  }
}