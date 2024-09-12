package com.axonivy.market.util;

import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ImageUtilsTest {

  @Test
  void testMappingImageForProductModuleContent() {
    String expectedValue = "Login or create a new account.[demo-process](/api/product-details/image/66e2b13c68f2f95b2f95548c)";
    var result = ImageUtils.mappingImageForProductModuleContent(mockProductModuleContent());
    Assertions.assertEquals(expectedValue, result.getDescription().get("en"));
    Assertions.assertEquals(expectedValue, result.getSetup().get("de"));
  }

  private ProductModuleContent mockProductModuleContent(){
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setDescription(mockDescriptionForProductModuleContent());
    productModuleContent.setDemo(null);
    productModuleContent.setSetup(mockDescriptionForProductModuleContent());

    return productModuleContent;
  }

  private Map<String, String> mockDescriptionForProductModuleContent(){
    Map<String, String> mutableMap = new HashMap<>();
    mutableMap.put("en", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    mutableMap.put("de", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    return mutableMap;

  }

}
