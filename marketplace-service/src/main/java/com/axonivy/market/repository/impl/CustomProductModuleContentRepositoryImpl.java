package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.repository.CustomProductModuleContentRepository;
import com.axonivy.market.repository.CustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.util.List;

@Builder
public class CustomProductModuleContentRepositoryImpl extends CustomRepository implements CustomProductModuleContentRepository {

  EntityManager em;

  @Override
  public List<String> findVersionsByProductId(String id) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> cq = cb.createQuery(String.class);
    Root<ProductModuleContent> root = cq.from(ProductModuleContent.class);
    cq.select(root.get("version")).where(cb.equal(root.get("productId"), id));
    return em.createQuery(cq).getResultList();
  }

}