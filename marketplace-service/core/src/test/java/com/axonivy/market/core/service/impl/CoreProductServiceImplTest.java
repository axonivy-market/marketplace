package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.enums.TypeOption;
import com.axonivy.market.core.repository.CoreGithubRepoRepository;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductDesignerInstallationRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.core.service.CoreProductMarketplaceDataService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.core.utils.CoreMavenUtils;
import com.axonivy.market.core.utils.CoreVersionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CoreProductServiceImplTest extends CoreBaseSetup {
  @Mock
  private CoreProductRepository productRepo;

  @Mock
  private CoreGithubRepoRepository coreGithubRepoRepository;

  @Mock
  private CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository;

  @Mock
  private CoreMetadataRepository coreMetadataRepo;

  @Mock
  private CoreProductMarketplaceDataRepository coreProductMarketplaceDataRepo;

  @Mock
  private CoreProductJsonContentRepository coreProductJsonContentRepo;

  @Mock
  private CoreProductMarketplaceDataService coreProductMarketplaceDataService;

  @Mock
  private CoreVersionService coreVersionService;

  @Spy
  @InjectMocks
  private CoreProductServiceImpl productService;

  private String language;
  private String keyword;
  private Page<Product> mockResultReturn;

  @BeforeEach
  void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "All products should match mock result");
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "Connector type products should match mock result");
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(2, result.getSize(), "Number of demo type products should match number of mock demo type result");
  }

  @Test
  void testFindAllProductsWithKeyword() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "All products should match mock result");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));

    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(new PageImpl<>(
        mockResultReturn.stream().filter(
            product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)).toList()));
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent(), "Result product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(new PageImpl<>(
        mockResultReturn.stream().filter(product -> product.getNames().get(Language.EN.getValue()).equals(
            SAMPLE_PRODUCT_NAME) && product.getType().equals(TypeOption.CONNECTORS.getCode())).toList()));
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, language, false,
        PAGEABLE);
    assertTrue(result.hasContent(), "Result connector type product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");
  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, language, false, simplePageable);
    assertEquals(result, mockResultReturn, "Product list from search query should match mock product list");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));
  }

  @Test
  void testFetchBestMatchProductDetailByIdAndVersion() {
    Product mockProduct = getMockProduct();
    Metadata mockMetadata = getMockMetadataWithVersions();
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    when(coreMetadataRepo.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(mockProduct);
    when(coreProductMarketplaceDataService.updateProductInstallationCount(MOCK_PRODUCT_ID)).thenReturn(
        mockProductMarketplaceData.getInstallationCount());
    Product result = productService.fetchBestMatchProductDetail(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, result,
        "Found best match product version should match mock product");
  }

  @Test
  void testGetProductByIdWithNewestReleaseVersion() {
    List<MavenArtifactVersion> mockMavenArtifactVersions = getMockMavenArtifactVersionWithData();
    Product mockProduct = getMockProduct();

    try (MockedStatic<  CoreMavenUtils> mockUtils = Mockito.mockStatic(CoreMavenUtils.class);
         MockedStatic<CoreVersionUtils> mockCoreVersionUtils = Mockito.mockStatic(CoreVersionUtils.class)) {
      mockUtils.when(() -> coreMavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(
          mockMavenArtifactVersions);
      when(CoreVersionUtils.extractAllVersions(mockMavenArtifactVersions, true))
          .thenReturn(List.of(MOCK_SNAPSHOT_VERSION));

      when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      when(coreProductJsonContentRepo.findByProductIdAndVersionIgnoreCase(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION))
          .thenReturn(List.of(getMockProductJsonContentContainMavenDropins()));

      Product result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result,
          "Product with newest release version should match mock product");

      when(coreMavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());
      when(productRepo.getReleasedVersionsById(MOCK_PRODUCT_ID)).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
      when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);
      when(CoreVersionUtils.getVersionsToDisplay(any(), any())).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
      result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
      assertEquals(mockProduct, result,
          "Product with newest release version should match mock product");
    }
  }

  @Test
  void testGetProductByIdWithNewestReleaseVersionWithEmptyArtifact() {
    Product mockProduct = getMockProduct();
    when(coreProductJsonContentRepo.findByProductIdAndVersionIgnoreCase(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION))
        .thenReturn(List.of(getMockProductJsonContentContainMavenDropins()));
    when(coreMavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());
    when(productRepo.getReleasedVersionsById(MOCK_PRODUCT_ID)).thenReturn(List.of(MOCK_SNAPSHOT_VERSION));
    when(productRepo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_SNAPSHOT_VERSION)).thenReturn(mockProduct);

    Product result = productService.getProductByIdWithNewestReleaseVersion(MOCK_PRODUCT_ID, true);
    assertEquals(mockProduct, result,
        "Product with newest release version and empty artifact should match mock product");
  }

  protected Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Map<String, String> name = new HashMap<>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.put(Language.EN.getValue(), SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new HashMap<>();
    name.put(Language.EN.getValue(), "Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }
}
