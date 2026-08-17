package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductModuleContent;
import com.axonivy.market.core.enums.DocumentField;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class ProductSearchRepositoryImplTest extends BaseSetup {
  private static final String LISTED_PRODUCT_ID = "case-process-viewer-utils";
  private static final String DOCUMENTED_PRODUCT_ID = "express-importer";
  private static final String PORTAL_PRODUCT_ID = "portal";
  private static final String LISTED_PRODUCT_NAME = "Case Process Viewer";
  private static final String LISTED_PRODUCT_VERSION = "13.2.3";

  @Autowired
  private ProductRepository repository;

  @Test
  void shouldFindAllProductsHaveDocument() {
    List<Product> products = repository.findAllProductsHaveDocument();

    assertThat(products).extracting(Product::getId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID);
  }

  @Test
  void shouldFindProductByKeywordInName() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setKeyword(LISTED_PRODUCT_NAME);

    Product product = repository.findByCriteria(criteria);

    assertThat(product).isNotNull();
    assertThat(product.getId()).isEqualTo(LISTED_PRODUCT_ID);
  }

  @Test
  void shouldFindProductByMarketDirectoryWhenFieldIsRestricted() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setFields(List.of(DocumentField.MARKET_DIRECTORY));
    criteria.setKeyword("market/utils/express-importer/");

    Product product = repository.findByCriteria(criteria);

    assertThat(product).isNotNull();
    assertThat(product.getId()).isEqualTo(DOCUMENTED_PRODUCT_ID);
  }

  @Test
  void shouldAttachModuleContentForRequestedVersion() {
    Product product = repository.getProductByIdAndVersion(LISTED_PRODUCT_ID, LISTED_PRODUCT_VERSION);

    assertThat(product).isNotNull();
    assertThat(product.getId()).isEqualTo(LISTED_PRODUCT_ID);

    ProductModuleContent content = product.getProductModuleContent();
    assertThat(content).isNotNull();
    assertThat(content.getProductId()).isEqualTo(LISTED_PRODUCT_ID);
    assertThat(content.getVersion()).isEqualTo(LISTED_PRODUCT_VERSION);
    assertThat(content.getDescription()).containsEntry("en",
        "This Axon Ivy component visually represents the process flow of your current case. It highlights both the active task and all completed tasks directly on the process diagram.");
    assertThat(content.getSetup()).containsEntry("en", "Add the Component to Your JSF Page");
    assertThat(content.getDemo()).containsEntry("en", "1. Start **Purchase Request Demo** process");
    assertThat(content.getComponent()).containsEntry("en", "");
  }
}
