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
  private static final String LISTED_PRODUCT_ID = "listed-product";
  private static final String HIDDEN_PRODUCT_ID = "hidden-product";
  private static final String DOCUMENTED_PRODUCT_ID = "documented-product";
  private static final String LISTED_ARTIFACT_ID = "listed-artifact";
  private static final String EN_LANGUAGE = "en";
  private static final String LISTED_PRODUCT_VERSION = "1.0.0";
  private static final String LISTED_PRODUCT_NAME = "Listed Product";
  private static final String LISTED_PRODUCT_SHORT_DESCRIPTION = "Listed product short description";
  private static final String LISTED_PRODUCT_DESCRIPTION = "Listed product description";
  private static final String LISTED_PRODUCT_SETUP = "Listed product setup";
  private static final String LISTED_PRODUCT_DEMO = "Listed product demo";
  private static final String LISTED_PRODUCT_COMPONENT = "Listed product component";

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
    Product product = productRepository.findProductByIdAndRelatedData(HIDDEN_PRODUCT_ID);
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
        .containsExactlyInAnyOrder(LISTED_PRODUCT_ID, DOCUMENTED_PRODUCT_ID);
  }
}
