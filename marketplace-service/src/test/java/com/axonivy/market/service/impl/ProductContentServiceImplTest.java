package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.service.ImageService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductContentServiceImplTest extends BaseSetup {
  private static final String EXTRACT_DIR_LOCATION = "src/test/resources/zip/data";
  @InjectMocks
  private ProductContentServiceImpl productContentService;
  @Mock
  private ProductJsonContentService productJsonContentService;
  @Mock
  private ImageService imageService;

  @Test
  void testUpdateDependencyContentsFromProductJson() throws IOException {
    List<Artifact> mockArtifacts = List.of(mock(Artifact.class));
    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockedMavenUtils.when(
          () -> MavenUtils.convertProductJsonToMavenProductInfo(Paths.get(EXTRACT_DIR_LOCATION))).thenReturn(
          mockArtifacts);
      when(
          MavenUtils.extractProductJsonContent(Paths.get(EXTRACT_DIR_LOCATION, ProductJsonConstants.PRODUCT_JSON_FILE)))
          .thenReturn(getMockProductJsonNodeContent());

      productContentService.updateDependencyContentsFromProductJson(new ProductModuleContent(), MOCK_PRODUCT_ID,
          EXTRACT_DIR_LOCATION);

      verify(productJsonContentService, times(1))
          .updateProductJsonContent(getMockProductJsonNodeContent(), new ProductModuleContent().getVersion(),
              ProductJsonConstants.VERSION_VALUE, MOCK_PRODUCT_ID);
    }
  }

  @Test
  void testUpdateImagesWithDownloadUrl() throws IOException {
    String readmeContent = getMockReadmeContent();
    String productId = MOCK_PRODUCT_ID;
    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      Path downloadLocation = Paths.get(EXTRACT_DIR_LOCATION);
      Path imagePath1 = Paths.get("screen1.png");
      Path imagePath2 = Paths.get("screen2.png");

      when(Files.walk(downloadLocation)).thenReturn(Stream.of(downloadLocation, imagePath1, imagePath2));
      mockedFiles.when(() -> Files.isRegularFile(imagePath1)).thenReturn(true);
      mockedFiles.when(() -> Files.isRegularFile(imagePath2)).thenReturn(true);

      when(imageService.mappingImageFromDownloadedFolder(productId, imagePath1)).thenReturn(getMockImage());
      when(imageService.mappingImageFromDownloadedFolder(productId, imagePath2)).thenReturn(getMockImage2());

      String result = productContentService.updateImagesWithDownloadUrl(productId, EXTRACT_DIR_LOCATION,
          readmeContent);
      String expectedResult = readmeContent.replace("screen1.png \"Screen 1\"",
          "imageId-66e2b14868f2f95b2f95549a").replace("screen2.png " + "\"Screen 2\"",
          "imageId-66e2b14868f2f95b2f95550a");
      assertEquals(expectedResult, result);
    }
  }
}