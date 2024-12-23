package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.model.ReadmeContentsModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    imageUrls.put("screen2.png", MOCK_IMAGE_ID_FORMAT_3);

    String expectedResult = readmeContents.replace("images/slash-command.png",
        MOCK_IMAGE_ID_FORMAT_1).replace("images/create-slash-command.png",
        MOCK_IMAGE_ID_FORMAT_2).replace("screen2.png \"Restful Person Manager\"", MOCK_IMAGE_ID_FORMAT_3);
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

  @Test
  void testGetExtractedPartsOfReadmeWithOnlyOneDescription() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(SAMPLE_PRODUCT_NAME);
    assertEquals(SAMPLE_PRODUCT_NAME, readmeContentsModel.getDescription());
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()));
  }

  @Test
  void testGetExtractedPartsOfEmptyReadme() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(StringUtils.EMPTY);
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDescription()));
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()));
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()));
  }

  @Test
  void testGetExtractedPartsOfReadmeAtCorrectHeadings() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(readmeContentsModel.getDescription().startsWith("Axon Ivy’s mattermost connector"));
    assertTrue(readmeContentsModel.getDemo().startsWith("### Demo sample"));
    assertTrue(readmeContentsModel.getSetup().startsWith("### Setup guideline"));
  }

  @Test
  void testGetExtractedPartsOfReadmeWithInconsistentFormats() {
    String readmeContentsWithHeading3 = """
        #Product-name
        Test README
        ### Setup
        Setup content (./image.png)""";
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContentsWithHeading3);
    assertTrue(readmeContentsModel.getDescription().startsWith("Test README"));
    assertTrue(readmeContentsModel.getSetup().startsWith("Setup content (./image.png)"));

    String readmeContentsWithSpaceHeading = """
        #Product-name
        Test README
        ##Demo
        ### Demo project
        Demo content
           ## Setup
        Setup content (./image.png)""";
    ReadmeContentsModel readmeContentsModel1 =
        ProductContentUtils.getExtractedPartsOfReadme(readmeContentsWithSpaceHeading);
    assertTrue(readmeContentsModel1.getDemo().startsWith("### Demo project"));
    assertTrue(readmeContentsModel1.getSetup().startsWith("Setup content (./image.png)"));
  }

  @Test
  void testHasImageDirectives() {
    String readmeContents = getMockReadmeContent();
    assertTrue(ProductContentUtils.hasImageDirectives(readmeContents));
    assertFalse(ProductContentUtils.hasImageDirectives(StringUtils.EMPTY));
  }

  @Test
  void testInitProductModuleContent() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId());
    assertEquals(MOCK_RELEASED_VERSION, productModuleContent.getVersion());
    assertEquals(String.format(CommonConstants.ID_WITH_NUMBER_PATTERN, SAMPLE_PRODUCT_ID, MOCK_RELEASED_VERSION),
        productModuleContent.getId());
  }

  @Test
  void testInitProductModuleContentWithoutVersion() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        StringUtils.EMPTY);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId());
    assertTrue(StringUtils.isBlank(productModuleContent.getVersion()));
    assertTrue(StringUtils.isBlank(productModuleContent.getId()));
  }

  @Test
  void testMappingDescriptionSetupDemoAndUpdateProductModuleTabContents() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    ProductContentUtils.mappingDescriptionSetupAndDemo(moduleContents, MOCK_README_FILE, readmeContentsModel);
    String readmeDEContents = getMockReadmeContent(MOCK_README_DE_FILE);
    ReadmeContentsModel readmeDEContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeDEContents);
    ProductContentUtils.mappingDescriptionSetupAndDemo(moduleContents, MOCK_README_DE_FILE, readmeDEContentsModel);
    ProductModuleContent productModuleContent = new ProductModuleContent();
    ProductContentUtils.updateProductModuleTabContents(productModuleContent, moduleContents);
    assertEquals(3, moduleContents.size());
    assertTrue(productModuleContent.getDescription().get(Language.EN.getValue()).startsWith("Axon Ivy"));
    assertTrue(productModuleContent.getDescription().get(Language.DE.getValue()).startsWith("Der"));
    assertTrue(StringUtils.isNotBlank(productModuleContent.getSetup().get(Language.DE.getValue())));
    assertTrue(StringUtils.equals(productModuleContent.getSetup().get(Language.DE.getValue()),
        productModuleContent.getSetup().get(Language.EN.getValue())));
    assertTrue(StringUtils.equals(productModuleContent.getSetup().get(Language.DE.getValue()),
        productModuleContent.getSetup().get(Language.EN.getValue())));
    assertTrue(StringUtils.isNotBlank(productModuleContent.getDemo().get(Language.DE.getValue())));
    assertTrue(StringUtils.equals(productModuleContent.getDemo().get(Language.DE.getValue()),
        productModuleContent.getDemo().get(Language.EN.getValue())));
    assertTrue(StringUtils.equals(productModuleContent.getDemo().get(Language.DE.getValue()),
        productModuleContent.getDemo().get(Language.EN.getValue())));
  }
}