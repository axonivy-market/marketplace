package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ProductModuleContent;
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
  @InjectMocks
  private ProductContentServiceImpl productContentService;
  @Mock
  private ProductJsonContentService productJsonContentService;

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

      productContentService.updateDependencyContentsFromProductJson(new ProductModuleContent(), getMockProduct(),
          EXTRACT_DIR_LOCATION);

      verify(productJsonContentService, times(1))
          .updateProductJsonContent(getMockProductJsonNodeContent(), new ProductModuleContent().getVersion(),
              ProductJsonConstants.VERSION_VALUE, getMockProduct());
    }
  }
}