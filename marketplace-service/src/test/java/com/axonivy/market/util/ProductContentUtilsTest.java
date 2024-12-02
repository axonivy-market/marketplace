package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.ProductModuleContent;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ProductContentUtilsTest extends BaseSetup {
  @Test
  void testUpdateProductModule() {
    ProductModuleContent mockProductModuleContent = new ProductModuleContent();
    Artifact mockArtifact = getMockArtifact();
    ProductContentUtils.updateProductModule(mockProductModuleContent, List.of(mockArtifact));
    assertEquals(mockArtifact.getGroupId(), mockProductModuleContent.getGroupId());
    assertEquals(mockArtifact.getArtifactId(), mockProductModuleContent.getArtifactId());
    assertEquals(mockArtifact.getName(), mockProductModuleContent.getName());
    assertEquals(mockArtifact.getType(), mockProductModuleContent.getType());
  }

  @Test
  void testRemoveFirstLine() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(null));
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(" "));
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine("#"));
    assertEquals("Second line", ProductContentUtils.removeFirstLine("#First line\nSecond line"));
  }

  @Test
  void testGetReadmeFileLocale() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.getReadmeFileLocale("README.md"));
    assertEquals("DE", ProductContentUtils.getReadmeFileLocale("README_DE.md"));
  }

  @Test
  void testReplaceImageDirWithImageCustomId() {
    String readmeContents = getMockReadmeContent();
    Map<String, String> imageUrls = new HashMap<>();
    imageUrls.put("slash-command.png", MOCK_IMAGE_ID_FORMAT_1);
    imageUrls.put("create-slash-command.png", MOCK_IMAGE_ID_FORMAT_2);

    String expectedResult = readmeContents.replace("images/slash-command.png",
        MOCK_IMAGE_ID_FORMAT_1).replace("images/create-slash-command.png",
        MOCK_IMAGE_ID_FORMAT_2);
    String updatedContents = ProductContentUtils.replaceImageDirWithImageCustomId(imageUrls, readmeContents);

    assertEquals(expectedResult, updatedContents);
  }
}