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
  void shouldUpdateInitialCount() {
    assertThat(repository.updateInitialCount(LISTED_PRODUCT_ID, 10)).isEqualTo(10);

    var updated = repository.findById(LISTED_PRODUCT_ID).orElseThrow();
    assertThat(updated.getInstallationCount()).isEqualTo(10);
    assertThat(updated.getSynchronizedInstallationCount()).isTrue();
  }

  @Test
  void shouldCreateMarketplaceDataWhenMissing() {
    repository.checkAndInitProductMarketplaceDataIfNotExist(NEW_PRODUCT_ID);

    var created = repository.findById(NEW_PRODUCT_ID).orElseThrow();
    assertThat(created.getInstallationCount()).isZero();
  }
}
