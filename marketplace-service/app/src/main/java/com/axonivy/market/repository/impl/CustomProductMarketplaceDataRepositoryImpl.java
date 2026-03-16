package com.axonivy.market.repository.impl;

import com.axonivy.market.core.constants.CorePostgresDBConstants;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.impl.CoreCustomProductMarketplaceDataRepositoryImpl;
import com.axonivy.market.repository.CustomProductMarketplaceDataRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CustomProductMarketplaceDataRepositoryImpl extends CoreCustomProductMarketplaceDataRepositoryImpl
    implements CustomProductMarketplaceDataRepository {

  @Override
  @Transactional
  public void checkAndInitProductMarketplaceDataIfNotExist(String productId) {
    CriteriaByTypeContext<ProductMarketplaceData, Long> criteriaNumberContext = createCriteriaTypeContext(Long.class);

    criteriaNumberContext.query().select(criteriaNumberContext.builder().count(criteriaNumberContext.root())).where(
        criteriaNumberContext.builder().equal(criteriaNumberContext.root().get(CorePostgresDBConstants.ID), productId));
    Long count = getEntityManager().createQuery(criteriaNumberContext.query()).getSingleResult();
    boolean marketPlaceExists = count > 0;
    if (!marketPlaceExists) {
      var productMarketplaceData = new ProductMarketplaceData();
      productMarketplaceData.setId(productId);
      save(productMarketplaceData);
    }
  }
}
