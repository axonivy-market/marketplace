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
  void testFindProductByIdAndRelatedData() {
    Product product = productRepository.findProductByIdAndRelatedData(LISTED_PRODUCT_ID);

    assertThat(product)
        .as("listed product should be resolved with related data")
        .isNotNull();
    assertThat(product.getId())
        .as("product id should match the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);
    assertThat(product.getNames())
        .as("product names should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_NAME);
    assertThat(product.getShortDescriptions())
        .as("short descriptions should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_SHORT_DESCRIPTION);
    assertThat(product.getArtifacts())
        .as("artifacts should contain the expected artifact id")
        .extracting(Artifact::getArtifactId)
        .containsExactly(LISTED_ARTIFACT_ID);
  }

  @Test
  void testReturnNullWhenProductIsNotListed() {
    Product product = productRepository.findProductByIdAndRelatedData(PORTAL_PRODUCT_ID);
    assertThat(product)
        .as("unlisted products should not be returned")
        .isNull();
  }

  @Test
  void testAttachProductModuleContentByVersion() {
    Product product = productRepository.getProductByIdAndVersion(LISTED_PRODUCT_ID, LISTED_PRODUCT_VERSION);
    assertThat(product)
        .as("listed product should be found by version")
        .isNotNull();

    ProductModuleContent content = product.getProductModuleContent();
    assertThat(content)
        .as("module content should be attached to the product")
        .isNotNull();
    assertThat(content.getProductId())
        .as("module content should belong to the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);
    assertThat(content.getVersion())
        .as("module content should match the requested version")
        .isEqualTo(LISTED_PRODUCT_VERSION);
    assertThat(content.getDescription())
        .as("description should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_DESCRIPTION);
    assertThat(content.getSetup())
        .as("setup should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_SETUP);
    assertThat(content.getDemo())
        .as("demo should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_DEMO);
    assertThat(content.getComponent())
        .as("component should include the English entry")
        .containsEntry(EN_LANGUAGE, LISTED_PRODUCT_COMPONENT);
  }

  @Test
  void testFindAllProductsHaveDocumentWithoutDuplicates() {
    List<Product> products = productRepository.findAllProductsHaveDocument();

    assertThat(products)
        .as("products with documents should be returned without duplicates")
        .extracting(Product::getId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID);
  }
}
