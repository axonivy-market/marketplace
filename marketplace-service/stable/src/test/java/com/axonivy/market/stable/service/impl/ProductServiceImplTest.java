package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.utils.CoreVersionUtils;
import com.axonivy.market.stable.factory.VersionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

  @Mock
  private CoreProductRepository coreProductRepository;

  @InjectMocks
  private ProductServiceImpl productService;

  @Test
  void shouldThrowExceptionWhenProductNotFound() {
    String id = "p1";

    when(coreProductRepository.existsById(id)).thenReturn(false);
    assertThrows(
        NotFoundException.class,
        () -> productService.getBestMatchVersion(id, "1.0", false),
        "Expected NotFoundException when product does not exist"
    );

    verify(coreProductRepository).existsById(id);
    verifyNoMoreInteractions(coreProductRepository);
  }

  @Test
  void shouldReturnBestMatchVersion() {
    String id = "p1";
    String requestedVersion = "1.0";
    Boolean showDev = false;

    List<String> rawVersions = List.of("1.0", "2.0");
    List<String> filteredVersions = List.of("2.0", "1.0");

    when(coreProductRepository.existsById(id)).thenReturn(true);
    when(coreProductRepository.getReleasedVersionsById(id)).thenReturn(rawVersions);

    try (MockedStatic<CoreVersionUtils> utilsMock = mockStatic(CoreVersionUtils.class);
         MockedStatic<VersionFactory> factoryMock = mockStatic(VersionFactory.class)) {

      utilsMock.when(() ->
          CoreVersionUtils.getVersionsToDisplay(rawVersions, showDev)
      ).thenReturn(filteredVersions);

      factoryMock.when(() ->
          VersionFactory.get(filteredVersions, requestedVersion)
      ).thenReturn("2.0");

      String result = productService.getBestMatchVersion(id, requestedVersion, showDev);

      assertEquals("2.0", result,
          "Expected best matched version from VersionFactory");

      utilsMock.verify(() ->
          CoreVersionUtils.getVersionsToDisplay(rawVersions, showDev)
      );

      factoryMock.verify(() ->
          VersionFactory.get(filteredVersions, requestedVersion)
      );
    }
  }
}
