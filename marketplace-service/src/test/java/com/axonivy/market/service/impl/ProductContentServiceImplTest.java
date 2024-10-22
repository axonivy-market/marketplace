package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductJsonContentService;
import com.axonivy.market.util.MavenUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductContentServiceImplTest extends BaseSetup {
  private static final String EXTRACT_DIR_LOCATION = "src/test/resources/zip/data";
  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";
  @InjectMocks
  private ProductContentServiceImpl productContentService;
  @Mock
  private FileDownloadService fileDownloadService;
  @Mock
  private ProductJsonContentService productJsonContentService;

  //TODO
//  @Test
//  void testGetReadmeAndProductContentsFromTag() throws IOException {
//    String version = "1.0.0";
//    Artifact mockArtifact = mock(Artifact.class);
//    Product mockProduct = mock(Product.class);
//
//    // Mock Product and its ID
//    when(mockProduct.getId()).thenReturn("mockProductId");
//
//    String readmeContentWithImageFolder = """
//        # Product-name
//        Test README
//        ## Demo
//        Demo content
//        ## Setup
//        Setup content (./image.png)""";
//
//    // Create a mock ProductModuleContent with expected values
//    ProductModuleContent mockProductModuleContent = new ProductModuleContent();
//    mockProductModuleContent.setTag("v1.0.0");
//    mockProductModuleContent.setDescription(Map.of(Language.EN.getValue(), "Test README"));
//    mockProductModuleContent.setDemo(Map.of(Language.EN.getValue(), "Demo content"));
//    mockProductModuleContent.setSetup(
//        Map.of(Language.EN.getValue(), "Setup content (imageId-66e2b14868f2f95b2f95549a)"));
//
//    // Mock static utility methods
//    try (MockedStatic<ProductContentUtils> mockedProductContentUtils = Mockito.mockStatic(ProductContentUtils
//    .class)) {
//      mockedProductContentUtils.when(() -> ProductContentUtils.initProductModuleContent("mockProductId", version))
//          .thenReturn(mockProductModuleContent);
//
//      // Mock file download and unzipping
//      when(fileDownloadService.downloadAndUnzipProductContentFile(DOWNLOAD_URL, mockArtifact)).thenReturn(
//          EXTRACT_DIR_LOCATION);
//
//      // Create a mock readme file and its corresponding content
//      Path readmeFilePath = Paths.get(EXTRACT_DIR_LOCATION, "README.md");
//      Files.createDirectories(readmeFilePath.getParent());
//      Files.writeString(readmeFilePath, readmeContentWithImageFolder);
//
//      // Mock MavenUtils methods
//      when(MavenUtils.convertProductJsonToMavenProductInfo(any(Path.class))).thenReturn(new ArrayList<>());
//      when(MavenUtils.extractProductJsonContent(any(Path.class))).thenReturn("{ \"mock\": \"json\" }");
//
//      // Call the method under test
//      ProductModuleContent result = productContentService.getReadmeAndProductContentsFromTag(mockProduct, version,
//          DOWNLOAD_URL,
//          mockArtifact);
//
//      // Verify that the returned result is not null and has the expected values
//      assertEquals("v1.0.0", result.getTag());
//      assertEquals("Test README", result.getDescription().get(Language.EN.getValue()));
//      assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
//      assertEquals("Setup content (imageId-66e2b14868f2f95b2f95549a)", result.getSetup().get(Language.EN.getValue()));
//
//      // Verify that the expected methods were called
//      verify(fileDownloadService, times(1)).downloadAndUnzipProductContentFile(DOWNLOAD_URL, mockArtifact);
//      verify(fileDownloadService, times(1)).deleteDirectory(Path.of(EXTRACT_DIR_LOCATION));
//
//    }
//  }

  @Test
  void testUpdateDependencyContentsFromProductJson() throws IOException {
    String mockUnzippedFolderPath = "mock/unzipped/folder/path";
    List<Artifact> mockArtifacts = List.of(mock(Artifact.class));

    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {

      mockedMavenUtils.when(
          () -> MavenUtils.convertProductJsonToMavenProductInfo(Paths.get(mockUnzippedFolderPath))).thenReturn(
          mockArtifacts);

      String mockProductJsonContent = "{\"version\": \"1.0.0\"}";
      when(
          MavenUtils.extractProductJsonContent(
              Paths.get(mockUnzippedFolderPath, ProductJsonConstants.PRODUCT_JSON_FILE)))
          .thenReturn(mockProductJsonContent);

      productContentService.updateDependencyContentsFromProductJson(new ProductModuleContent(), getMockProduct(),
          mockUnzippedFolderPath);

      verify(productJsonContentService, times(1))
          .updateProductJsonContent(eq(mockProductJsonContent), isNull(), eq(new ProductModuleContent().getTag()),
              eq(ProductJsonConstants.VERSION_VALUE), eq(getMockProduct()));
    }
  }

}