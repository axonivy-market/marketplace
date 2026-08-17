package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class CustomProductDesignerInstallationRepositoryImplTest {
  private static final String PRODUCT_ID = "express-importer";
  private static final String EXISTING_VERSION = "10.0.22";
  private static final String NEW_VERSION = "11.4.0";

  @Autowired
  private ProductDesignerInstallationRepository repository;

  @Autowired
  private EntityManager entityManager;

  @Test
  void shouldIncreaseInstallationCountForExistingVersion() {
    repository.increaseInstallationCountForProductByDesignerVersion(PRODUCT_ID, EXISTING_VERSION);
    repository.flush();
    entityManager.clear();

    ProductDesignerInstallation installation = findInstallation(EXISTING_VERSION);
    assertThat(installation.getInstallationCount()).isEqualTo(3);
  }

  @Test
  void shouldCreateNewInstallationWhenVersionDoesNotExist() {
    repository.increaseInstallationCountForProductByDesignerVersion(PRODUCT_ID, NEW_VERSION);
    repository.flush();
    entityManager.clear();

    ProductDesignerInstallation installation = findInstallation(NEW_VERSION);
    assertThat(installation.getInstallationCount()).isEqualTo(1);
  }

  private ProductDesignerInstallation findInstallation(String version) {
    return repository.findByProductId(PRODUCT_ID, Sort.by("designerVersion")).stream()
        .filter(installation -> version.equals(installation.getDesignerVersion()))
        .findFirst()
        .orElseThrow();
  }
}
