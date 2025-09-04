package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepo;

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
    assertEquals(1, capturedProducts.size(),
        "Product list size should be 1");
    assertEquals(1, capturedProducts.get(0).getCustomOrder(),
        "Product list custom order should be 1");
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));

    List<ProductMarketplaceData> refinedProducts =
        productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts);

    assertEquals(1, refinedProducts.size(),
        "Product list size should be 1");
    assertEquals(1, refinedProducts.get(0).getCustomOrder(),
        "Product list custom order should be 1");
    verify(productMarketplaceDataRepo).findById(SAMPLE_PRODUCT_ID);
  }

  @Test
  void testRefineOrderedListOfProductsInCustomSortProductNotFound() {
    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts));
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
        "Should throw product not found exception when no product is found");
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

    assertEquals(10, result,
        "Installation count should match 10 when not synchronized");
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
    assertEquals(4, result, "Installation count should match 4");

    result = productMarketplaceDataService.updateInstallationCountForProduct(SAMPLE_PRODUCT_ID, StringUtils.EMPTY);
    assertEquals(4, result, "Installation count should match 4");
  }

  @Test
  void testSyncInstallationCountWithNewProduct() {
    ProductMarketplaceData mockProductMarketplaceData = ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).build();
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertTrue(installationCount >= 20 && installationCount <= 50,
        "Installation count should be more than 20 and less than 50");
  }

  @Test
  void testGetInstallationCountFromFileOrInitializeRandomly() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();

    int installationCount = productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(
        mockProductMarketplaceData.getId());

    assertEquals(40, installationCount,
        "Installation count should match 40 from file");
  }

  @Test
  void testBuildArtifactStreamFromResource() {
    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
        INSTALLATION_FILE_PATH);
    when(productRepo.findById(anyString())).thenReturn(Optional.of(getMockProduct()));
    OutputStream result = productMarketplaceDataService.buildArtifactStreamFromResource(MOCK_DOWNLOAD_URL,
        getMockResource(), new ByteArrayOutputStream());
    assertNotNull(result, "Artifact stream should not be null with existed artifact");
  }

  @Test
  void testGetProductArtifactStreamShouldReturnResource() {
    MavenArtifactVersion mav = new MavenArtifactVersion();
    mav.setDownloadUrl(MOCK_DOWNLOAD_URL);
    when(mavenArtifactVersionRepo.findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .thenReturn(List.of(mav));
    ByteArrayResource resource = new ByteArrayResource("data".getBytes());
    ResponseEntity<Resource> responseEntity = ResponseEntity.ok(resource);
    when(fileDownloadService.fetchUrlResource(MOCK_DOWNLOAD_URL)).thenReturn(responseEntity);

    ResponseEntity<Resource> result = productMarketplaceDataService.getProductArtifactStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION);

    assertNotNull(result, "Result stream should not be null with existed artifact");
    assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode(), "Response entity should return code of 200");
    assertEquals(result.getBody(), resource, "Response's body should equal to the content form received stream");
    verify(mavenArtifactVersionRepo).findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION);
    verify(fileDownloadService).fetchUrlResource(MOCK_DOWNLOAD_URL);
  }

}