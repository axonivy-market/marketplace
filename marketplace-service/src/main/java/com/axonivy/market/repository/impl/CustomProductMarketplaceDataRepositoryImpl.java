package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.repository.CustomProductMarketplaceDataRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

import static com.axonivy.market.constants.PostgresDBConstants.*;

@Builder
@AllArgsConstructor
public class CustomProductMarketplaceDataRepositoryImpl implements CustomProductMarketplaceDataRepository {

  private static final String INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID = "UPDATE product_marketplace_data SET " +
      "installation_count = installation_count + 1 WHERE id = :productId RETURNING installation_count";

  private static final String INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID_FOR_DESIGNER_VERSION = "UPDATE " +
      "product_designer_installation SET installation_count = installation_count + 1 WHERE product_id = :productId " +
      "and" +
      " designer_version = :designerVersion";

  EntityManager em;

  @Override
  @Transactional
  public int updateInitialCount(String productId, int initialCount) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaUpdate<ProductMarketplaceData> updateQuery = cb.createCriteriaUpdate(ProductMarketplaceData.class);
    Root<ProductMarketplaceData> root = updateQuery.from(ProductMarketplaceData.class);

    // Set the fields
    updateQuery.set(root.get(INSTALLATION_COUNT), initialCount);
    updateQuery.set(root.get(SYNCHRONIZED_INSTALLATION_COUNT), true);
    // Where condition (filter by productId)
    updateQuery.where(cb.equal(root.get(ID), productId));
    // Execute the update
    int updatedRows = em.createQuery(updateQuery).executeUpdate();
    em.clear();
    // Fetch the updated entity if needed
    if (updatedRows > 0) {
      return em.find(ProductMarketplaceData.class, productId).getInstallationCount();
    }
    return 0;
  }

  @Override
  @Transactional
  public int increaseInstallationCount(String productId) {
    Query query = em.createNativeQuery(INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID);
    query.setParameter(PRODUCT_ID, productId);
    return ((Number) query.getSingleResult()).intValue();
  }

  @Override
  @Transactional
  public void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ProductDesignerInstallation> cbQuery = cb.createQuery(ProductDesignerInstallation.class);
    Root<ProductDesignerInstallation> productDesignerInstallationRoot = cbQuery.from(ProductDesignerInstallation.class);

    cbQuery.where(cb.equal(productDesignerInstallationRoot.get(PRODUCT_ID), productId),
        cb.equal(productDesignerInstallationRoot.get(DESIGNER_VERSION), designerVersion));

    List<ProductDesignerInstallation> existsDesignerInstallation = em.createQuery(cbQuery).getResultList();

    if (ObjectUtils.isEmpty(existsDesignerInstallation)) {
      ProductDesignerInstallation installation = new ProductDesignerInstallation();
      installation.setProductId(productId);
      installation.setDesignerVersion(designerVersion);
      installation.setInstallationCount(1);
      em.persist(installation);
    } else {
      Query query = em.createNativeQuery(INCREASE_INSTALLATION_COUNT_VIA_PRODUCT_ID_FOR_DESIGNER_VERSION);
      query.setParameter(PRODUCT_ID, productId);
      query.setParameter(DESIGNER_VERSION, designerVersion);
      query.executeUpdate();
    }
  }


  @Override
  @Transactional
  public void checkAndInitProductMarketplaceDataIfNotExist(String productId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<ProductMarketplaceData> root = query.from(ProductMarketplaceData.class);
    query.select(cb.count(root)).where(cb.equal(root.get(ID), productId));
    Long count = em.createQuery(query).getSingleResult();
    boolean marketPlaceExists = count > 0;
    if (!marketPlaceExists) {
      ProductMarketplaceData productMarketplaceData = new ProductMarketplaceData();
      productMarketplaceData.setId(productId);
      em.persist(productMarketplaceData);
    }
  }
}
