package com.axonivy.market.core.repository.impl;

import static com.axonivy.market.core.constants.CorePostgresDBConstants.DESIGNER_VERSION;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.PRODUCT_ID;

import com.axonivy.market.core.entity.ProductDesignerInstallation;
import com.axonivy.market.core.repository.CoreAbstractBaseRepository;
import com.axonivy.market.core.repository.CoreCustomProductDesignerInstallationRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class CoreCustomProductDesignerInstallationRepositoryImpl extends CoreAbstractBaseRepository<ProductDesignerInstallation>
    implements CoreCustomProductDesignerInstallationRepository {
  private static final String INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID_FOR_DESIGNER_VERSION = """
      UPDATE product_designer_installation 
      SET installation_count = installation_count + 1 
      WHERE product_id = :productId 
      AND designer_version = :designerVersion
      """;

  @Override
  @Transactional
  public void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion) {
    CriteriaQueryContext<ProductDesignerInstallation> criteriaQueryContext = createCriteriaQueryContext();

    criteriaQueryContext.query().where(criteriaQueryContext.builder().equal(criteriaQueryContext.root().get(PRODUCT_ID),
            productId),
        criteriaQueryContext.builder().equal(criteriaQueryContext.root().get(DESIGNER_VERSION), designerVersion));

    List<ProductDesignerInstallation> existsDesignerInstallation = findByCriteria(criteriaQueryContext);

    if (ObjectUtils.isEmpty(existsDesignerInstallation)) {
      var installation = new ProductDesignerInstallation();
      installation.setProductId(productId);
      installation.setDesignerVersion(designerVersion);
      installation.setInstallationCount(1);
      save(installation);
    } else {
      var query = getEntityManager().createNativeQuery(
          INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID_FOR_DESIGNER_VERSION);
      query.setParameter(PRODUCT_ID, productId);
      query.setParameter(DESIGNER_VERSION, designerVersion);
      query.executeUpdate();
    }
  }

  @Override
  protected Class<ProductDesignerInstallation> getType() {
    return ProductDesignerInstallation.class;
  }
}
