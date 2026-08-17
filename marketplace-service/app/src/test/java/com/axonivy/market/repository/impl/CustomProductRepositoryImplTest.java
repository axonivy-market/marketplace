package com.axonivy.market.repository.impl;

import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductModuleContent;
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
class CustomProductRepositoryImplTest {
  private static final String LISTED_PRODUCT_ID = "case-process-viewer-utils";
  private static final String PORTAL_PRODUCT_ID = "portal";
  private static final String DOCUMENTED_PRODUCT_ID = "express-importer";
  private static final String LISTED_ARTIFACT_ID = "case-process-viewer-utils-product";
  private static final String EN_LANGUAGE = "en";
  private static final String LISTED_PRODUCT_VERSION = "13.2.3";
  private static final String LISTED_PRODUCT_NAME = "Case Process Viewer";
  private static final String LISTED_PRODUCT_SHORT_DESCRIPTION =
      "This Axon Ivy utility visualizes the current progress of a running process by highlighting the active task as well as all completed tasks directly within the process diagram.";
  private static final String LISTED_PRODUCT_DESCRIPTION =
      "This Axon Ivy component visually represents the process flow of your current case. It highlights both the active task and all completed tasks directly on the process diagram.";
  private static final String LISTED_PRODUCT_SETUP = "Add the Component to Your JSF Page";
  private static final String LISTED_PRODUCT_DEMO = "1. Start **Purchase Request Demo** process";
  private static final String LISTED_PRODUCT_COMPONENT = "";

  @Autowired
  private ProductRepository productRepository;

  @Test
  void shouldFindProductByIdAndRelatedData() {
    Product product = productRepository.findProductByIdAndRelatedData(LISTED_PRODUCT_ID);

    assertThat(product).isNotNull();
    assertThat(product.getId()).isEqualTo(LISTED_PRODUCT_ID);
    assertThat(product.getNames()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_NAME);
    assertThat(product.getShortDescriptions()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_SHORT_DESCRIPTION);
    assertThat(product.getArtifacts()).extracting(Artifact::getArtifactId).containsExactly(LISTED_ARTIFACT_ID);
  }

  @Test
  void shouldReturnNullWhenProductIsNotListed() {
    Product product = productRepository.findProductByIdAndRelatedData(PORTAL_PRODUCT_ID);
    assertThat(product).isNull();
  }

  @Test
  void shouldAttachProductModuleContentByVersion() {
    Product product = productRepository.getProductByIdAndVersion(LISTED_PRODUCT_ID, LISTED_PRODUCT_VERSION);
    assertThat(product).isNotNull();

    ProductModuleContent content = product.getProductModuleContent();
    assertThat(content).isNotNull();
    assertThat(content.getProductId()).isEqualTo(LISTED_PRODUCT_ID);
    assertThat(content.getVersion()).isEqualTo(LISTED_PRODUCT_VERSION);
    assertThat(content.getDescription()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_DESCRIPTION);
    assertThat(content.getSetup()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_SETUP);
    assertThat(content.getDemo()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_DEMO);
    assertThat(content.getComponent()).containsEntry(EN_LANGUAGE, LISTED_PRODUCT_COMPONENT);
  }

  @Test
  void shouldFindAllProductsHaveDocumentWithoutDuplicates() {
    List<Product> products = productRepository.findAllProductsHaveDocument();

    assertThat(products)
        .extracting(Product::getId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID);
  }
}
