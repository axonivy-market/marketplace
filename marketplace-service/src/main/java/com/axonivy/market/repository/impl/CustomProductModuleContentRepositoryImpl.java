package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.CustomProductModuleContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Builder;

import java.util.List;

import static com.axonivy.market.constants.PostgresDBConstants.PRODUCT_ID;
import static com.axonivy.market.constants.PostgresDBConstants.VERSION;

@Builder
public class CustomProductModuleContentRepositoryImpl implements CustomProductModuleContentRepository {

  EntityManager em;

  @Override
  public List<String> findVersionsByProductId(String id) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> cq = cb.createQuery(String.class);
    Root<ProductModuleContent> root = cq.from(ProductModuleContent.class);
    cq.select(root.get(VERSION)).where(cb.equal(root.get(PRODUCT_ID), id));
    return em.createQuery(cq).getResultList();
  }

}