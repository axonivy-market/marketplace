package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.model.ReadmeContentsModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void testGetExtractedPartsOfReadme() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()));
  }

  @Test
  void testGetExtractedPartsOfReadmeSwapDemoAndSetupParts() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_SWAP_DEMO_SETUP_PARTS);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()));
    if (StringUtils.isNotBlank(readmeContentsModel.getSetup())) {
      assertTrue(readmeContentsModel.getSetup().startsWith("Mattermost Instance"));
    }
  }

  @Test
  void testGetExtractedPartsOfReadmeNoDemoPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_DEMO_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()));
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()));
  }

  @Test
  void testGetExtractedPartsOfReadmeNoSetupPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_SETUP_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()));
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()));
  }
}