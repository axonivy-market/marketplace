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
import org.kohsuke.github.GHRelease;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProductContentUtilsTest extends BaseSetup {
  @Test
  void testUpdateProductModule() {
    ProductModuleContent mockProductModuleContent = new ProductModuleContent();
    Artifact mockArtifact = getMockArtifact();
    ProductContentUtils.updateProductModule(mockProductModuleContent, List.of(mockArtifact));
    assertEquals(mockArtifact.getGroupId(), mockProductModuleContent.getGroupId(),
        "Group ID should be updated from artifact");
    assertEquals(mockArtifact.getArtifactId(), mockProductModuleContent.getArtifactId(),
        "Artifact ID should be updated from artifact");
    assertEquals(mockArtifact.getName(), mockProductModuleContent.getName(), "Name should be updated from artifact");
    assertEquals(mockArtifact.getType(), mockProductModuleContent.getType(), "Type should be updated from artifact");
  }

  @Test
  void testRemoveFirstLine() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(null), "Null input should return empty string");
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine(" "),
        "Whitespace input should return empty string");
    assertEquals(StringUtils.EMPTY, ProductContentUtils.removeFirstLine("#"),
        "Single hash input should return empty string");
    assertEquals("Second line", ProductContentUtils.removeFirstLine("#First line\nSecond line"),
        "Should remove first line and return remaining content");
  }

  @Test
  void testGetReadmeFileLocale() {
    assertEquals(StringUtils.EMPTY, ProductContentUtils.getReadmeFileLocale("README.md"),
        "Default README.md should return empty locale");
    assertEquals("DE", ProductContentUtils.getReadmeFileLocale("README_DE.md"), "README_DE.md should return DE locale");
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

    assertEquals(expectedResult, updatedContents, "Image directories should be replaced with custom image IDs");
  }

  @Test
  void testGetExtractedPartsOfReadme() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()), "Description should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()), "Demo section should not be blank");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()), "Setup section should not be blank");
  }

  @Test
  void testGetExtractedPartsOfReadmeSwapDemoAndSetupParts() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_SWAP_DEMO_SETUP_PARTS);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank for swapped demo/setup parts");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()),
        "Demo section should not be blank for swapped demo/setup parts");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()),
        "Setup section should not be blank for swapped demo/setup parts");
    if (StringUtils.isNotBlank(readmeContentsModel.getSetup())) {
      assertTrue(readmeContentsModel.getSetup().startsWith("Mattermost Instance"),
          "Setup section should start with 'Mattermost Instance' for swapped parts");
    }
  }

  @Test
  void testGetExtractedPartsOfReadmeNoDemoPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_DEMO_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank when demo part is missing");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()),
        "Demo section should be blank when demo part is missing");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getSetup()),
        "Setup section should not be blank when demo part is missing");
  }

  @Test
  void testGetExtractedPartsOfReadmeNoSetupPart() {
    String readmeContents = getMockReadmeContent(MOCK_README_FILE_NO_SETUP_PART);
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDescription()),
        "Description should not be blank when setup part is missing");
    assertTrue(StringUtils.isNotBlank(readmeContentsModel.getDemo()),
        "Demo section should not be blank when setup part is missing");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()),
        "Setup section should be blank when setup part is missing");
  }

  @Test
  void testGetExtractedPartsOfReadmeWithOnlyOneDescription() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(SAMPLE_PRODUCT_NAME);
    assertEquals(SAMPLE_PRODUCT_NAME, readmeContentsModel.getDescription(),
        "Description should match the sample product name");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()),
        "Demo section should be blank for single description");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()),
        "Setup section should be blank for single description");
  }

  @Test
  void testGetExtractedPartsOfEmptyReadme() {
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(StringUtils.EMPTY);
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDescription()),
        "Description should be blank for empty readme");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getDemo()), "Demo section should be blank for empty readme");
    assertTrue(StringUtils.isBlank(readmeContentsModel.getSetup()), "Setup section should be blank for empty readme");
  }

  @Test
  void testGetExtractedPartsOfReadmeAtCorrectHeadings() {
    String readmeContents = getMockReadmeContent();
    ReadmeContentsModel readmeContentsModel = ProductContentUtils.getExtractedPartsOfReadme(readmeContents);
    assertTrue(readmeContentsModel.getDescription().startsWith("Axon Ivy’s mattermost connector"),
        "Description should start with expected content");
    assertTrue(readmeContentsModel.getDemo().startsWith("### Demo sample"),
        "Demo section should start with '### Demo sample'");
    assertTrue(readmeContentsModel.getSetup().startsWith("### Setup guideline"),
        "Setup section should start with '### Setup guideline'");
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
        "Description should start with 'Test README' for heading3 format");
    assertTrue(readmeContentsModel.getSetup().startsWith("Setup content (./image.png)"),
        "Setup section should start with expected content for heading3 format");

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
        "Demo section should start with '### Demo project' for space heading format");
    assertTrue(readmeContentsModel1.getSetup().startsWith("Setup content (./image.png)"),
        "Setup section should start with expected content for space heading format");
  }

  @Test
  void testHasImageDirectives() {
    String readmeContents = getMockReadmeContent();
    assertTrue(ProductContentUtils.hasImageDirectives(readmeContents),
        "Mock readme content should contain image directives");
    assertFalse(ProductContentUtils.hasImageDirectives(StringUtils.EMPTY),
        "Empty string should not contain image directives");
  }

  @Test
  void testInitProductModuleContent() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId(),
        "Product ID should match the sample product ID");
    assertEquals(MOCK_RELEASED_VERSION, productModuleContent.getVersion(),
        "Version should match the mock released version");
    assertEquals(String.format(CommonConstants.ID_WITH_NUMBER_PATTERN, SAMPLE_PRODUCT_ID, MOCK_RELEASED_VERSION),
        productModuleContent.getId(), "ID should be formatted with product ID and version pattern");
  }

  @Test
  void testInitProductModuleContentWithoutVersion() {
    ProductModuleContent productModuleContent = ProductContentUtils.initProductModuleContent(SAMPLE_PRODUCT_ID,
        StringUtils.EMPTY);
    assertEquals(SAMPLE_PRODUCT_ID, productModuleContent.getProductId(),
        "Product ID should match the sample product ID when version is empty");
    assertTrue(StringUtils.isBlank(productModuleContent.getVersion()),
        "Version should be blank when empty version is provided");
    assertTrue(StringUtils.isBlank(productModuleContent.getId()), "ID should be blank when version is empty");
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
    assertEquals(3, moduleContents.size(), "Module contents should contain 3 sections (description, demo, setup)");
    assertTrue(productModuleContent.getDescription().get(Language.EN.getValue()).startsWith("Axon Ivy"),
        "English description should start with 'Axon Ivy'");
    assertTrue(productModuleContent.getDescription().get(Language.DE.getValue()).startsWith("Der"),
        "German description should start with 'Der'");
    assertTrue(StringUtils.isNotBlank(productModuleContent.getSetup().get(Language.DE.getValue())),
        "German setup content should not be blank");
    assertTrue(StringUtils.equals(productModuleContent.getSetup().get(Language.DE.getValue()),
            productModuleContent.getSetup().get(Language.EN.getValue())),
        "German and English setup content should be equal");
    assertTrue(StringUtils.equals(productModuleContent.getSetup().get(Language.DE.getValue()),
            productModuleContent.getSetup().get(Language.EN.getValue())),
        "German and English setup content should be equal (duplicate check)");
    assertTrue(StringUtils.isNotBlank(productModuleContent.getDemo().get(Language.DE.getValue())),
        "German demo content should not be blank");
    assertTrue(StringUtils.equals(productModuleContent.getDemo().get(Language.DE.getValue()),
        productModuleContent.getDemo().get(Language.EN.getValue())), "German and English demo content should be equal");
    assertTrue(StringUtils.equals(productModuleContent.getDemo().get(Language.DE.getValue()),
            productModuleContent.getDemo().get(Language.EN.getValue())),
        "German and English demo content should be equal (duplicate check)");
  }

  @Test
  void testTransformGithubReleaseBody() {
    String githubReleaseBody = "This is a release body with PR #123 and user @johndoe";
    String productSourceUrl = "http://example.com";

    String result = ProductContentUtils.transformGithubReleaseBody(githubReleaseBody, productSourceUrl);

    assertNotNull(result, "Transformed release body should not be null");
    assertTrue(result.contains("http://example.com/pull/123"), "Result should contain transformed PR link");
    assertTrue(result.contains("https://github.com/johndoe"), "Result should contain transformed user link");
  }

  @Test
  void testExtractReleasesPageWithNormalPagination() {
    List<GHRelease> releases = createMockReleases(10);
    Pageable pageable = PageRequest.of(0, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(3, result.size(), "First page should contain 3 releases");
    assertEquals(releases.get(0), result.get(0), "First release should match first item in page");
    assertEquals(releases.get(1), result.get(1), "Second release should match second item in page");
    assertEquals(releases.get(2), result.get(2), "Third release should match third item in page");
  }

  @Test
  void testExtractReleasesPageWithSecondPage() {
    List<GHRelease> releases = createMockReleases(10);
    Pageable pageable = PageRequest.of(1, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(3, result.size(), "Second page should contain 3 releases");
    assertEquals(releases.get(3), result.get(0), "First release in second page should be 4th release overall");
    assertEquals(releases.get(4), result.get(1), "Second release in second page should be 5th release overall");
    assertEquals(releases.get(5), result.get(2), "Third release in second page should be 6th release overall");
  }

  @Test
  void testExtractReleasesPageWithLastPagePartial() {
    List<GHRelease> releases = createMockReleases(8);
    Pageable pageable = PageRequest.of(2, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(2, result.size(), "Last partial page should contain 2 releases");
    assertEquals(releases.get(6), result.get(0), "First release in last page should be 7th release overall");
    assertEquals(releases.get(7), result.get(1), "Second release in last page should be 8th release overall");
  }

  @Test
  void testExtractReleasesPageWithStartBeyondList() {
    List<GHRelease> releases = createMockReleases(5);
    Pageable pageable = PageRequest.of(3, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertTrue(result.isEmpty(), "Page beyond list bounds should return empty list");
  }

  @Test
  void testExtractReleasesPageWithEmptyList() {
    List<GHRelease> releases = Collections.emptyList();
    Pageable pageable = PageRequest.of(0, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertTrue(result.isEmpty(), "Empty input list should return empty result");
  }

  @Test
  void testExtractReleasesPageWithExactPageSize() {
    List<GHRelease> releases = createMockReleases(3);
    Pageable pageable = PageRequest.of(0, 3);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(3, result.size(), "Exact page size should return all releases");
    assertEquals(releases, result, "Result should match original list for exact page size");
  }

  @Test
  void testExtractReleasesPageWithSingleItemPage() {
    List<GHRelease> releases = createMockReleases(5);
    Pageable pageable = PageRequest.of(2, 1);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(1, result.size(), "Single item page should return one release");
    assertEquals(releases.get(2), result.get(0), "Single item page should return the correct release");
  }

  @Test
  void testExtractReleasesPageWithLargePage() {
    List<GHRelease> releases = createMockReleases(5);
    Pageable pageable = PageRequest.of(0, 10);

    List<GHRelease> result = ProductContentUtils.extractReleasesPage(releases, pageable);

    assertEquals(5, result.size(), "Large page size should return all available releases");
    assertEquals(releases, result, "Result should match original list when page size exceeds list size");
  }

  private List<GHRelease> createMockReleases(int count) {
    List<GHRelease> releases = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      releases.add(mock(GHRelease.class));
    }
    return releases;
  }
}