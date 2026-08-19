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
  void testFindAllProductsHaveDocument() {
    List<Product> products = repository.findAllProductsHaveDocument();

    assertThat(products)
        .as("documents should only exist for the expected products")
        .extracting(Product::getId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID);
  }

  @Test
  void testFindProductByKeywordInName() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setKeyword(LISTED_PRODUCT_NAME);

    Product product = repository.findByCriteria(criteria);

    assertThat(product)
        .as("keyword search should return the matching product")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the keyword result")
        .isEqualTo(LISTED_PRODUCT_ID);
  }

  @Test
  void testFindProductByMarketDirectoryWhenFieldIsRestricted() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setFields(List.of(DocumentField.MARKET_DIRECTORY));
    criteria.setKeyword("market/utils/express-importer/");

    Product product = repository.findByCriteria(criteria);

    assertThat(product)
        .as("restricted field search should still return the matching product")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the market directory lookup")
        .isEqualTo(DOCUMENTED_PRODUCT_ID);
  }

  @Test
  void testAttachModuleContentForRequestedVersion() {
    Product product = repository.getProductByIdAndVersion(LISTED_PRODUCT_ID, LISTED_PRODUCT_VERSION);

    assertThat(product)
        .as("product should be returned for the requested version")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);

    ProductModuleContent content = product.getProductModuleContent();
    assertThat(content)
        .as("module content should be attached for the requested version")
        .isNotNull();
    assertThat(content.getProductId())
        .as("module content should belong to the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);
    assertThat(content.getVersion())
        .as("module content version should match the request")
        .isEqualTo(LISTED_PRODUCT_VERSION);
    assertThat(content.getDescription())
        .as("description should include the English entry")
        .containsEntry("en",
        "This Axon Ivy component visually represents the process flow of your current case. It highlights both the active task and all completed tasks directly on the process diagram.");
    assertThat(content.getSetup())
        .as("setup should include the English entry")
        .containsEntry("en", "Add the Component to Your JSF Page");
    assertThat(content.getDemo())
        .as("demo should include the English entry")
        .containsEntry("en", "1. Start **Purchase Request Demo** process");
    assertThat(content.getComponent())
        .as("component should include the English entry")
        .containsEntry("en", "");
  }
}
