package com.axonivy.market.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.ProductModuleContent;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductContentUtilsTest {
  @Test
  void testUpdateProductModule() {
    ProductModuleContent mockProductModuleContent = new ProductModuleContent();
    Artifact mockArtifact = new Artifact();
    mockArtifact.setIsDependency(true);
    mockArtifact.setGroupId("com.axonivy.utils");
    mockArtifact.setArtifactId("octopus");
    mockArtifact.setType("zip");
    mockArtifact.setName("Octopus demo (zip)");
    ProductContentUtils.updateProductModule(mockProductModuleContent, List.of(mockArtifact));
    Assertions.assertEquals(mockArtifact.getGroupId(), mockProductModuleContent.getGroupId());
    Assertions.assertEquals(mockArtifact.getArtifactId(), mockProductModuleContent.getArtifactId());
    Assertions.assertEquals(mockArtifact.getName(), mockProductModuleContent.getName());
    Assertions.assertEquals(mockArtifact.getType(), mockProductModuleContent.getType());
  }

  @Test
  void testRemoveFirstLine() {
    Assertions.assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(null));
    Assertions.assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(" "));
    Assertions.assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine("#"));
    Assertions.assertEquals("Second line", ProductContentUtils.removeFirstLine("#First line\nSecond line"));
  }

  @Test
  void testGetReadmeFileLocale() {
    Assertions.assertEquals(StringUtils.EMPTY, ProductContentUtils.getReadmeFileLocale("README.md"));
    Assertions.assertEquals("DE", ProductContentUtils.getReadmeFileLocale("README_DE.md"));
  }
}