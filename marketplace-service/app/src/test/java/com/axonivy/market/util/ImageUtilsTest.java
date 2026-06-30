package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.entity.ProductModuleContent;
import com.axonivy.market.testutil.MockServletRequestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class ImageUtilsTest extends BaseSetup {

  @BeforeEach
  void setupRequestContext() {
    MockServletRequestUtils.createAndBindMockRequest();
  }

  @AfterEach
  void resetRequestContext() {
    MockServletRequestUtils.resetRequestAttributes();
  }

  @Test
  void testMappingImageForProductModuleContent() {
    String expectedValue = "Login or create a new account.[demo-process](/api/image/66e2b13c68f2f95b2f95548c)";
    var result = ImageUtils.mappingImageForProductModuleContent(mockProductModuleContent(), true);
    Assertions.assertEquals(expectedValue, result.getDescription().get("en"),
        "Product module content description should match input");
    Assertions.assertEquals(expectedValue, result.getSetup().get("de"),
        "Product module content setup should match input");
  }

  private ProductModuleContent mockProductModuleContent() {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setDescription(mockDescriptionForProductModuleContent());
    productModuleContent.setDemo(null);
    productModuleContent.setSetup(mockDescriptionForProductModuleContent());

    return productModuleContent;
  }

  private Map<String, String> mockDescriptionForProductModuleContent() {
    Map<String, String> mutableMap = new HashMap<>();
    mutableMap.put("en", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    mutableMap.put("de", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    return mutableMap;
  }

}
