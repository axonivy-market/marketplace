package com.axonivy.market.core.repository.impl;

import com.axonivy.market.core.constants.CorePostgresDBConstants;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.CoreAbstractBaseRepository;
import jakarta.transaction.Transactional;

public class CoreCustomProductMarketplaceDataRepositoryImpl extends CoreAbstractBaseRepository<ProductMarketplaceData>
    implements CustomProductMarketplaceDataRepository {
  @Override
  @Transactional
  public int updateInitialCount(String productId, int initialCount) {
    CoreAbstractBaseRepository.CriteriaUpdateContext<ProductMarketplaceData> criteriaUpdateContext = createCriteriaUpdateContext();
    // Set the fields
    criteriaUpdateContext.query().set(criteriaUpdateContext.root().get(CorePostgresDBConstants.INSTALLATION_COUNT),
        initialCount);
    criteriaUpdateContext.query().set(
        criteriaUpdateContext.root().get(PostgresDBConstants.SYNCHRONIZED_INSTALLATION_COUNT), true);
    // Where condition (filter by productId)
    criteriaUpdateContext.query().where(
        criteriaUpdateContext.builder().equal(criteriaUpdateContext.root().get(CorePostgresDBConstants.ID),
            productId));
    // Execute the update
    int updatedRows = executeQuery(criteriaUpdateContext);
    getEntityManager().clear();
    // Fetch the updated entity if needed
    if (updatedRows > 0) {
      return getEntityManager().find(getType(), productId).getInstallationCount();
    }
    return 0;
  }
}
