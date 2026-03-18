package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.CoreProductDesignerInstallationRepository;
import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import org.apache.commons.lang3.StringUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CoreProductMarketplaceDataServiceImplTest extends CoreBaseSetup {
  @Mock
  private CoreProductRepository coreProductRepo;

  @Mock
  private CoreProductMarketplaceDataRepository coreProductMarketplaceDataRepo;

  @Mock
  private CoreProductDesignerInstallationRepository coreProductDesignerInstallationRepo;

  @InjectMocks
  private CoreProductMarketplaceDataServiceImpl coreProductMarketplaceDataService;

  @Test
  void testUpdateInstallationCountForProduct() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(true);
    ReflectionTestUtils.setField(coreProductMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
            INSTALLATION_FILE_PATH);

    when(coreProductRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(new Product()));
    when(coreProductMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID))
            .thenReturn(Optional.of(mockProductMarketplaceData));
    when(coreProductMarketplaceDataRepo.increaseInstallationCount(SAMPLE_PRODUCT_ID)).thenReturn(4);

    int result = coreProductMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID,
            MOCK_RELEASED_VERSION);
    assertEquals(4, result, "Installation count should match 4");

    result = coreProductMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID, StringUtils.EMPTY);
    assertEquals(4, result, "Installation count should match 4");
  }

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(false);
    ReflectionTestUtils.setField(coreProductMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(coreProductMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID))
            .thenReturn(Optional.of(mockProductMarketplaceData));
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
