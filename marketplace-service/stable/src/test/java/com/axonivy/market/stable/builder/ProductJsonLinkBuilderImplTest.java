package com.axonivy.market.stable.builder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProductJsonLinkBuilderImplTest {
  private ProductJsonLinkBuilderImpl productJsonLinkBuilder;

  @BeforeEach
  void setup() {
    productJsonLinkBuilder = new ProductJsonLinkBuilderImpl();
  }

  @Test
  void testBuildProductJsonUrl() {
    String productId = "bpmn-statistic";
    String version = "11.3.0-SNAPSHOT";
    String designerVersion = "12.0.4";

    String result = productJsonLinkBuilder.buildProductJsonUrl(
        productId,
        version,
        designerVersion
    );

    assertTrue(result.endsWith(
            "/api/product-details/bpmn-statistic/11.3.0-SNAPSHOT/json?designerVersion=12.0.4"
        )
    );
  }
}
