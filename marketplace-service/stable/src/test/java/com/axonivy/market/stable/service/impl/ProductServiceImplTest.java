package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.utils.CoreVersionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {
  @Mock
  private CoreMetadataRepository coreMetadataRepo;

  @InjectMocks
  private ProductServiceImpl productService;

  @Test
  void shouldReturnBestMatchVersion() {
    String productId = "test-product";
    String inputVersion = "10.0.0";

    List<Metadata> metadataList = List.of(new Metadata(), new Metadata());
    List<String> installableVersions = List.of("9.0.0", "10.0.0", "11.0.0");

    when(coreMetadataRepo.findByProductId(productId)).thenReturn(metadataList);

    try (MockedStatic<CoreVersionUtils> mockedStatic = mockStatic(CoreVersionUtils.class)) {
      mockedStatic.when(() ->
              CoreVersionUtils.getInstallableVersionsFromMetadataList(metadataList))
          .thenReturn(installableVersions);
      mockedStatic.when(() ->
              CoreVersionUtils.getBestMatchVersion(installableVersions, inputVersion))
          .thenReturn("10.0.0");

      String result = productService.fetchBestMatchVersion(productId, inputVersion);
      assertEquals("10.0.0", result,
          "Expected best match version to be '10.0.0' for input version '10.0.0'");
    }
  }
}
