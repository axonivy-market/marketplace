package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.repository.CustomProductMarketplaceDataRepository;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.Builder;

import static com.axonivy.market.constants.PostgresDBConstants.*;

@Builder
public class CustomProductMarketplaceDataRepositoryImpl extends BaseRepository<ProductMarketplaceData> implements CustomProductMarketplaceDataRepository {

  private static final String INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID = """
          UPDATE product_marketplace_data  
          SET installation_count = installation_count + 1 
          WHERE id = :productId 
          RETURNING installation_count
      """;

  @Override
  @Transactional
  public int updateInitialCount(String productId, int initialCount) {
    CriteriaUpdateContext<ProductMarketplaceData> criteriaUpdateContext = createCriteriaUpdateContext();
    // Set the fields
    criteriaUpdateContext.query().set(criteriaUpdateContext.root().get(INSTALLATION_COUNT), initialCount);
    criteriaUpdateContext.query().set(criteriaUpdateContext.root().get(SYNCHRONIZED_INSTALLATION_COUNT), true);
    // Where condition (filter by productId)
    criteriaUpdateContext.query().where(criteriaUpdateContext.builder().equal(criteriaUpdateContext.root().get(ID), productId));
    // Execute the update
    int updatedRows = executeQuery(criteriaUpdateContext);
    entityManager.clear();
    // Fetch the updated entity if needed
    if (updatedRows > 0) {
      return entityManager.find(getType(), productId).getInstallationCount();
    }
    return 0;
  }

  @Override
  @Transactional
  public int increaseInstallationCount(String productId) {
    Query query = entityManager.createNativeQuery(INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID);
    query.setParameter(PRODUCT_ID, productId);
    return ((Number) query.getSingleResult()).intValue();
  }

  @Override
  @Transactional
  public void checkAndInitProductMarketplaceDataIfNotExist(String productId) {
    CriteriaByTypeContext<ProductMarketplaceData,Long> criteriaNumberContext = createCriteriaTypeContext(Long.class);

    criteriaNumberContext.query().select(criteriaNumberContext.builder().count(criteriaNumberContext.root()))
        .where(criteriaNumberContext.builder().equal(criteriaNumberContext.root().get(ID), productId));
    Long count = entityManager.createQuery(criteriaNumberContext.query()).getSingleResult();
    boolean marketPlaceExists = count > 0;
    if (!marketPlaceExists) {
      ProductMarketplaceData productMarketplaceData = new ProductMarketplaceData();
      productMarketplaceData.setId(productId);
      save(productMarketplaceData);
    }
  }

  @Override
  protected Class<ProductMarketplaceData> getType() {
    return ProductMarketplaceData.class;
  }
}
