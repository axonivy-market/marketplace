package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.ProductMarketplaceData;

import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CoreProductMarketplaceDataServiceImplTest extends CoreBaseSetup {
  @Mock
  private CoreProductMarketplaceDataRepository coreProductMarketplaceDataRepo;

  @InjectMocks
  private CoreProductMarketplaceDataServiceImpl coreProductMarketplaceDataService;

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(false);
    ReflectionTestUtils.setField(coreProductMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(coreProductMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(coreProductMarketplaceDataRepo.updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt())).thenReturn(10);

    int result = coreProductMarketplaceDataService.updateProductInstallationCount(SAMPLE_PRODUCT_ID);

    assertEquals(10, result,
        "Installation count should match 10 when not synchronized");
    verify(coreProductMarketplaceDataRepo).updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt());
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    ProductMarketplaceData mockProductMarketplaceData = ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).build();
    ReflectionTestUtils.setField(coreProductMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    int installationCount = coreProductMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertTrue(installationCount >= 20 && installationCount <= 50,
        "Installation count should be more than 20 and less than 50");
  }

  @Test
  void testGetInstallationCountFromFileOrInitializeRandomly() {
    ReflectionTestUtils.setField(coreProductMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();

    int installationCount = coreProductMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertEquals(40, installationCount,
        "Installation count should match 40 from file");
  }
}
