package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.repository.ProductModuleContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class CustomProductModuleContentRepositoryImplTest {
  private static final String LISTED_PRODUCT_ID = "case-process-viewer-utils";

  @Autowired
  private ProductModuleContentRepository repository;

  @Test
  void shouldFindVersionsByProductId() {
    assertThat(repository.findVersionsByProductId(LISTED_PRODUCT_ID)).containsExactly("13.2.3");
  }
}
