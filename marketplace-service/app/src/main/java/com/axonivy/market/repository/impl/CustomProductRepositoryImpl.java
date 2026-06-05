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

import java.util.List;
import java.util.Objects;

import static com.axonivy.market.constants.PostgresDBConstants.DOC;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.*;

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
        Hibernate.initialize(content.getComponent());
        result.setProductModuleContent(content);
      }
    }
    return result;
  }

  @Override
  public Product findProductByIdAndRelatedData(String id) {
    CriteriaQueryContext<Product> context = createCriteriaQueryContext();
    var root = context.root();
    var cb = context.builder();
    root.fetch(PRODUCT_NAMES, JoinType.LEFT);
    root.fetch(PRODUCT_SHORT_DESCRIPTION, JoinType.LEFT);
    root.fetch(PRODUCT_ARTIFACT, JoinType.LEFT);
    var idPredicate = cb.equal(root.get(ID), id);
    var listedPredicate = cb.or(
        cb.notEqual(root.get(LISTED), false),
        cb.isNull(root.get(LISTED)));
    context.query().where(idPredicate, listedPredicate);
    try {
      return getEntityManager().createQuery(context.query()).getSingleResult();
    } catch (NoResultException e) {
      log.error("Cannot find product: ", e);
      return null;
    }
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
