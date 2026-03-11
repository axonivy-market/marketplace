package com.axonivy.market.core.repository.impl;

import com.axonivy.market.core.constants.CorePostgresDBConstants;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.CoreAbstractBaseRepository;
import com.axonivy.market.core.repository.CoreCustomProductMarketplaceDataRepository;
import jakarta.transaction.Transactional;
import lombok.Builder;

@Builder
public class CoreCustomProductMarketplaceDataRepositoryImpl extends CoreAbstractBaseRepository<ProductMarketplaceData>
    implements CoreCustomProductMarketplaceDataRepository {

//  private static final String INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID = """
//          UPDATE product_marketplace_data
//          SET installation_count = installation_count + 1
//          WHERE id = :productId
//          RETURNING installation_count
//      """;

  @Override
  @Transactional
  public int updateInitialCount(String productId, int initialCount) {
    CoreAbstractBaseRepository.CriteriaUpdateContext<ProductMarketplaceData> criteriaUpdateContext = createCriteriaUpdateContext();
    // Set the fields
    criteriaUpdateContext.query().set(criteriaUpdateContext.root().get(CorePostgresDBConstants.INSTALLATION_COUNT),
        initialCount);
    criteriaUpdateContext.query().set(
        criteriaUpdateContext.root().get(CorePostgresDBConstants.SYNCHRONIZED_INSTALLATION_COUNT), true);
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

  @Override
  protected Class<ProductMarketplaceData> getType() {
    return ProductMarketplaceData.class;
  }
}
