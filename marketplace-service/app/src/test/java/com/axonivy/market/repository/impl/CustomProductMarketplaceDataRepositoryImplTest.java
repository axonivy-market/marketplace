package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class CustomProductMarketplaceDataRepositoryImplTest {
  private static final String LISTED_PRODUCT_ID = "express-importer";
  private static final String NEW_PRODUCT_ID = "new-product";

  @Autowired
  private ProductMarketplaceDataRepository repository;

  @Test
  void testUpdateInitialCount() {
    assertThat(repository.updateInitialCount(LISTED_PRODUCT_ID, 10))
        .as("updateInitialCount should return the updated count")
        .isEqualTo(10);

    var updated = repository.findById(LISTED_PRODUCT_ID).orElseThrow();
    assertThat(updated.getInstallationCount())
        .as("installation count should be updated")
        .isEqualTo(10);
    assertThat(updated.getSynchronizedInstallationCount())
        .as("synchronized flag should be enabled after updating the initial count")
        .isTrue();
  }

  @Test
  void testCreateMarketplaceDataWhenMissing() {
    repository.checkAndInitProductMarketplaceDataIfNotExist(NEW_PRODUCT_ID);

    var created = repository.findById(NEW_PRODUCT_ID).orElseThrow();
    assertThat(created.getInstallationCount())
        .as("new marketplace data should start with zero installations")
        .isZero();
  }
}
