package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMarketplaceDataServiceImplTest extends BaseSetup {
  @Mock
  private ProductRepository productRepo;
  @Mock
  private ProductCustomSortRepository productCustomSortRepo;
  @Mock
  private ProductMarketplaceDataRepository productMarketplaceDataRepo;
  @Mock
  private FileDownloadService fileDownloadService;
  @Mock
  private ProductDesignerInstallationRepository productDesignerInstallationRepo;
  @InjectMocks
  private ProductMarketplaceDataServiceImpl productMarketplaceDataService;
  @Captor
  ArgumentCaptor<ArrayList<ProductMarketplaceData>> productListArgumentCaptor;

  @Test
  void testAddCustomSortProduct() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    ProductCustomSortRequest customSortRequest = new ProductCustomSortRequest();
    customSortRequest.setOrderedListOfProducts(orderedListOfProducts);
    customSortRequest.setRuleForRemainder(SortOption.ALPHABETICALLY.getOption());

    ProductMarketplaceData mockProductMarketplaceData = new ProductMarketplaceData();
    mockProductMarketplaceData.setId(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));

    productMarketplaceDataService.addCustomSortProduct(customSortRequest);

    verify(productCustomSortRepo).deleteAll();
    verify(productMarketplaceDataRepo).resetCustomOrderForAllProducts();
    verify(productCustomSortRepo).save(any(ProductCustomSort.class));
    verify(productMarketplaceDataRepo).saveAll(productListArgumentCaptor.capture());

    List<ProductMarketplaceData> capturedProducts = productListArgumentCaptor.getValue();
    assertEquals(1, capturedProducts.size());
    assertEquals(1, capturedProducts.get(0).getCustomOrder());
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));

    List<ProductMarketplaceData> refinedProducts =
        productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts);

    assertEquals(1, refinedProducts.size());
    assertEquals(1, refinedProducts.get(0).getCustomOrder());
    verify(productMarketplaceDataRepo).findById(SAMPLE_PRODUCT_ID);
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort_ProductNotFound() {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts));
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
  }

  @Test
  void testUpdateProductInstallationCountWhenNotSynchronized() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(false);
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(productMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataRepo.updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt())).thenReturn(10);

    int result = productMarketplaceDataService.updateProductInstallationCount(SAMPLE_PRODUCT_ID);

    assertEquals(10, result);
    verify(productMarketplaceDataRepo).updateInitialCount(eq(SAMPLE_PRODUCT_ID), anyInt());
  }

  @Test
  void testUpdateInstallationCountForProduct() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockProductMarketplaceData.setSynchronizedInstallationCount(true);
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(new Product()));
    when(productMarketplaceDataRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataRepo.increaseInstallationCount(SAMPLE_PRODUCT_ID)).thenReturn(4);

    int result = productMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(4, result);

    result = productMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID, StringUtils.EMPTY);
    assertEquals(4, result);
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    ProductMarketplaceData mockProductMarketplaceData = ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).build();
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertTrue(installationCount >= 20 && installationCount <= 50);
  }

  @Test
  void testGetInstallationCountFromFileOrInitializeRandomly() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertEquals(40, installationCount);
  }

  @Test
  void testDownloadArtifact() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    byte[] mockFileData = "dummy data".getBytes();
    when(fileDownloadService.safeDownload(MOCK_DOWNLOAD_URL)).thenReturn(mockFileData);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));
    when(productMarketplaceDataRepo.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(mockProductMarketplaceData));
    when(productMarketplaceDataService.updateInstallationCountForProduct(MOCK_PRODUCT_ID,
        MOCK_DESIGNER_VERSION)).thenReturn(4);
    VersionDownload result = productMarketplaceDataService.downloadArtifact(MOCK_DOWNLOAD_URL, MOCK_PRODUCT_ID);

    assertNotNull(result);
    assertEquals(4, result.getInstallationCount());
  }

  @Test
  void testSafeDownload_FileNotFound() {
    when(fileDownloadService.safeDownload(MOCK_DOWNLOAD_URL)).thenReturn(null);

    VersionDownload result = productMarketplaceDataService.downloadArtifact(MOCK_DOWNLOAD_URL, MOCK_PRODUCT_ID);
    assertNull(result);
    verify(fileDownloadService).safeDownload(MOCK_DOWNLOAD_URL);
  }

}