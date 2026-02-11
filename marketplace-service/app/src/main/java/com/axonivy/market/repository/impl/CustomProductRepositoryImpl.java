package com.axonivy.market.repository.impl;

import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductModuleContent;
import com.axonivy.market.core.repository.impl.CoreCustomProductRepositoryImpl;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Hibernate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.PostgresDBConstants.*;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.ID;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.PRODUCT_NAMES;

@Log4j2
public class CustomProductRepositoryImpl extends CoreCustomProductRepositoryImpl implements CustomProductRepository {
  private final ProductModuleContentRepository contentRepository;

  public CustomProductRepositoryImpl(ProductCustomSortRepository productCustomSortRepo,
      ProductModuleContentRepository contentRepository) {
    super(productCustomSortRepo);
    this.contentRepository = contentRepository;
  }

  @Override
  public Product getProductByIdAndVersion(String id, String version) {
    var result = findProductByIdAndRelatedData(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByVersionAndProductId(version, id);
      if (content != null) {
        Hibernate.initialize(content.getDescription());
        Hibernate.initialize(content.getSetup());
        Hibernate.initialize(content.getDemo());
        result.setProductModuleContent(content);
      }
    }
    return result;
  }

  @Override
  public Product findProductByIdAndRelatedData(String id) {
    CriteriaQueryContext<Product> context = createCriteriaQueryContext();
    context.root().fetch(PRODUCT_NAMES, JoinType.LEFT);
    context.root().fetch(PRODUCT_SHORT_DESCRIPTION, JoinType.LEFT);
    context.root().fetch(PRODUCT_ARTIFACT, JoinType.LEFT);
    context.query().where(context.builder().equal(context.root().get(ID), id));
    try {
      return getEntityManager().createQuery(context.query()).getSingleResult();
    } catch (NoResultException e) {
      log.error("Cannot find product: ", e);
      return null;
    }
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    return Optional.ofNullable(findProductByIdAndRelatedData(id))
        .map(Product::getReleasedVersions)
        .orElse(Collections.emptyList());
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    CriteriaQueryContext<Product> criteriaContext = createCriteriaQueryContext();
    Join<Product, Artifact> artifact = criteriaContext.root().join(PRODUCT_ARTIFACT);
    criteriaContext.query().select(criteriaContext.root()).distinct(true).where(
        criteriaContext.builder().isTrue(artifact.get(DOC)));

    return findByCriteria(criteriaContext);
  }
}
