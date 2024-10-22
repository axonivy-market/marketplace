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