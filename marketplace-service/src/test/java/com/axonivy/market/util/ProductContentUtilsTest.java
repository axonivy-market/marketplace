package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
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
    assertEquals(mockArtifact.getGroupId(), mockProductModuleContent.getGroupId(),
        "Product module content groupId should match artifact groupId");
    assertEquals(mockArtifact.getArtifactId(), mockProductModuleContent.getArtifactId(),
        "Product module content artifactId should match artifactId");
    assertEquals(mockArtifact.getName(), mockProductModuleContent.getName(),
        "Product module content name should match artifact name");
    assertEquals(mockArtifact.getType(), mockProductModuleContent.getType(),
        "Product module content type should match artifact type");
  }

  @Test
  void testRemoveFirstLine() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(null),
        "Null input should return empty string");
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(" "),
        "Whitespace input should return empty string");
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine("#"),
        "Single hash input should return empty string");
    assertEquals("Second line", ProductContentUtils.removeFirstLine("#First line\nSecond line"),
        "Should remove first line and return 'Second line'");
  }

  @Test
  void testGetReadmeFileLocale() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.getReadmeFileLocale("README.md"),
        "README.md should return empty locale");
    assertEquals("DE", ProductContentUtils.getReadmeFileLocale("README_DE.md"),
        "README_DE.md should return 'DE'");
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

    assertEquals(expectedResult, updatedContents,
        "Image URLs should be replaced with corresponding custom IDs");
  }

  @Test
  void testGetExtractedPartsOfReadme() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()),
        "Demo should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()),
        "Setup should not be blank");
  }

  @Test
  void testGetExtractedPartsOfReadmeSwapDemoAndSetupParts() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_SWAP_DEMO_SETUP_PARTS);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()),
        "Demo should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()),
        "Setup should not be blank");
    if (StringUtils.isNotBlank(readmeContentsModel.getSetup())) {
      assertTrue(readmeContentsModel.getSetup().startsWith("Mattermost Instance"),
          "Setup part should start with 'Mattermost Instance'");
    }
  }

  @Test
  void testGetExtractedPartsOfReadmeNoDemoPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_DEMO_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()),
        "Demo should be blank when no demo part exists");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()),
        "Setup should not be blank");
  }

  @Test
  void testGetExtractedPartsOfReadmeNoSetupPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_SETUP_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()),
        "Demo should be blank when no demo part exists");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()),
        "Setup should be blank when no setup part exists");
  }

  @Test
  void testGetExtractedPartsOfReadmeWithOnlyOneDescription() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(SAMPLE_PRODUCT_NAME);
    assertEquals(SAMPLE_PRODUCT_NAME, readmeContentsModel.getDescription(),
        "Description should equal sample product name");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()),
        "Demo should be blank");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()),
        "Setup should be blank");
  }

  @Test
  void testGetExtractedPartsOfEmptyReadme() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(StringUtils.EMPTY);
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDescription()),
        "Description should be blank for empty README");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()),
        "Demo should be blank for empty README");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()),
        "Setup should be blank for empty README");
  }

  @Test
  void testGetExtractedPartsOfReadmeAtCorrectHeadings() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(readmeContentsModel.getDescription().startsWith("Axon Ivy’s mattermost connector"),
        "Description should start with expected text");
    assertTrue(readmeContentsModel.getDemo().startsWith("### Demo sample"),
        "Demo should start with expected heading");
    assertTrue(readmeContentsModel.getSetup().startsWith("### Setup guideline"),
        "Setup should start with expected heading");
  }

  @Test
  void testGetExtractedPartsOfReadmeWithInconsistentFormats() {
    String readmeContentsWithHeading3 = """
        #Product-name
        Test README
        ### Setup
        Setup content (./image.png)""";
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContentsWithHeading3);
    assertTrue(readmeContentsModel.getDescription().startsWith("Test README"),
        "Description should start with 'Test README'");
    assertTrue(readmeContentsModel.getSetup().startsWith("Setup content (./image.png)"),
        "Setup should start with expected content");

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
    assertTrue(readmeContentsModel1.getDemo().startsWith("### Demo project"),
        "Demo should start with '### Demo project'");
    assertTrue(readmeContentsModel1.getSetup().startsWith("Setup content (./image.png)"),
        "Setup should start with expected content");
  }

  @Test
  void testHasImageDirectives() {
    String readmeContents = getMockReadmeContent();
    assertTrue(ProductContentUtils.hasImageDirectives(readmeContents),
        "Should detect image directives in README");
    assertFalse(ProductContentUtils.hasImageDirectives(StringUtils.EMPTY),
        "Empty README should not have image directives");
  }

  @Test
  void testInitProductModuleContent() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId(),
        "ProductId should match input");
    assertEquals(MOCK_RELEASED_VERSION, productModuleContent.getVersion(),
        "Version should match input");
    assertEquals(String.format(CommonConstants.ID_WITH_NUMBER_PATTERN, SAMPLE_PRODUCT_ID, MOCK_RELEASED_VERSION),
        productModuleContent.getId(),
        "Id should match expected format");
  }

  @Test
  void testInitProductModuleContentWithoutVersion() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        StringUtils.EMPTY);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId(),
        "ProductId should match input");
    assertTrue(StringUtils.isBlank(productModuleContent.getVersion()),
        "Version should be blank when input is empty");
    assertTrue(StringUtils.isBlank(productModuleContent.getId()),
        "Id should be blank when version is empty");
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

    assertEquals(3, moduleContents.size(),
        "Module contents should contain 3 entries");
    assertTrue(productModuleContent.getDescription().get(Language.EN.getValue()).startsWith("Axon Ivy"),
        "English description should start with 'Axon Ivy'");
    assertTrue(productModuleContent.getDescription().get(Language.DE.getValue()).startsWith("Der"),
        "German description should start with 'Der'");
    assertTrue(StringUtils.isNotBlank(productModuleContent.getSetup().get(Language.DE.getValue())),
        "German setup should not be blank");
    assertTrue(StringUtils.equals(productModuleContent.getSetup().get(Language.DE.getValue()),
        productModuleContent.getSetup().get(Language.EN.getValue())),
        "German and English setup should be equal");
    assertTrue(StringUtils.isNotBlank(productModuleContent.getDemo().get(Language.DE.getValue())),
        "German demo should not be blank");
    assertTrue(StringUtils.equals(productModuleContent.getDemo().get(Language.DE.getValue()),
        productModuleContent.getDemo().get(Language.EN.getValue())),
        "German and English demo should be equal");
  }
}