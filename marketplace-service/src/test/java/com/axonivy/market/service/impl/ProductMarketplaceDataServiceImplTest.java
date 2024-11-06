//package com.axonivy.market.service.impl;
//
//import com.axonivy.market.BaseSetup;
//import com.axonivy.market.entity.Product;
//import com.axonivy.market.entity.ProductCustomSort;
//import com.axonivy.market.enums.ErrorCode;
//import com.axonivy.market.enums.SortOption;
//import com.axonivy.market.exceptions.model.InvalidParamException;
//import com.axonivy.market.model.ProductCustomSortRequest;
//import com.axonivy.market.repository.ProductCustomSortRepository;
//import org.apache.commons.lang3.StringUtils;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.hamcrest.Matchers.any;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ProductMarketplaceDataServiceImplTest extends BaseSetup {
//  @Mock
//  private MongoTemplate mongoTemplate;
//  @Mock
//  private ProductCustomSortRepository productCustomSortRepo;
//  @InjectMocks
//  private ProductMarketplaceDataServiceImpl productMarketplaceDataService;
//  @Captor
//  ArgumentCaptor<ArrayList<Product>> productListArgumentCaptor;
//  private static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
//
//  @Test
//  void testRemoveFieldFromAllProductDocuments() {
//    productMarketplaceDataService.removeFieldFromAllProductDocuments("customOrder");
//
//    verify(mongoTemplate).updateMulti(ArgumentMatchers.any(Query.class), ArgumentMatchers.any(Update.class),
//        eq(Product.class));
//  }
//
//  @Test
//  void testAddCustomSortProduct() throws InvalidParamException {
//    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
//    ProductCustomSortRequest customSortRequest = new ProductCustomSortRequest();
//    customSortRequest.setOrderedListOfProducts(orderedListOfProducts);
//    customSortRequest.setRuleForRemainder(SortOption.ALPHABETICALLY.getOption());
//
//    Product mockProduct = new Product();
//    mockProduct.setId(SAMPLE_PRODUCT_ID);
//    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
//
//    productMarketplaceDataService.addCustomSortProduct(customSortRequest);
//
//    verify(productCustomSortRepo).deleteAll();
//    verify(mongoTemplate).updateMulti(any(Query.class), any(Update.class), eq(Product.class));
//    verify(productCustomSortRepo).save(any(ProductCustomSort.class));
//    verify(productRepo).saveAll(productListArgumentCaptor.capture());
//
//    List<Product> capturedProducts = productListArgumentCaptor.getValue();
//    assertEquals(1, capturedProducts.size());
//    assertEquals(1, capturedProducts.get(0).getCustomOrder());
//  }
//
//  @Test
//  void testRefineOrderedListOfProductsInCustomSort() throws InvalidParamException {
//    // prepare
//    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
//    Product mockProduct = new Product();
//    mockProduct.setId(SAMPLE_PRODUCT_ID);
//    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
//
//    List<Product> refinedProducts = productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(
//        orderedListOfProducts);
//
//    assertEquals(1, refinedProducts.size());
//    assertEquals(1, refinedProducts.get(0).getCustomOrder());
//    verify(productRepo).findById(SAMPLE_PRODUCT_ID);
//  }
//
//  @Test
//  void testRefineOrderedListOfProductsInCustomSort_ProductNotFound() {
//    List<String> orderedListOfProducts = List.of(SAMPLE_PRODUCT_ID);
//    when(productRepo.findById(SAMPLE_PRODUCT_ID)).thenReturn(Optional.empty());
//
//    InvalidParamException exception = assertThrows(InvalidParamException.class,
//        () -> productMarketplaceDataService.refineOrderedListOfProductsInCustomSort(orderedListOfProducts));
//    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
//  }
//
//  @Test
//  void testUpdateProductInstallationCountWhenNotSynchronized() {
//    Product product = getMockProduct();
//    product.setSynchronizedInstallationCount(false);
//    String id = product.getId();
//    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
//        INSTALLATION_FILE_PATH);
//
//    when(productRepo.updateInitialCount(eq(id), anyInt())).thenReturn(10);
//
//    productMarketplaceDataService.updateProductInstallationCount(id);
//
//    assertEquals(10, product.getInstallationCount());
//  }
//
//  @Test
//  void testUpdateInstallationCountForProduct() {
//    int result = productMarketplaceDataService.updateInstallationCountForProduct(null, MOCK_RELEASED_VERSION);
//    assertEquals(0, result);
//
//    Product product = getMockProduct();
//    product.setSynchronizedInstallationCount(true);
//    when(productRepo.getProductWithModuleContent(MOCK_PRODUCT_ID)).thenReturn(product);
//    when(productRepo.increaseInstallationCount(MOCK_PRODUCT_ID)).thenReturn(31);
//
//    result = productMarketplaceDataService.updateInstallationCountForProduct(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
//    assertEquals(31, result);
//
//    result = productMarketplaceDataService.updateInstallationCountForProduct(MOCK_PRODUCT_ID, StringUtils.EMPTY);
//    assertEquals(31, result);
//  }
//
//  @Test
//  void testSyncInstallationCountWithNewProduct() {
//    Product product = new Product();
//    product.setSynchronizedInstallationCount(null);
//    product.setId(MOCK_PRODUCT_ID);
//    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
//        INSTALLATION_FILE_PATH);
//
//    productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(product.getId());
//
//    assertTrue(product.getInstallationCount() >= 20 && product.getInstallationCount() <= 50);
//    assertTrue(product.getSynchronizedInstallationCount());
//  }
//
//  @Test
//  void testGetInstallationCountFromFileOrInitializeRandomly() {
//    ReflectionTestUtils.setField(productMarketplaceDataService, LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME,
//        INSTALLATION_FILE_PATH);
//    Product product = getMockProduct();
//    product.setSynchronizedInstallationCount(false);
//
//    productMarketplaceDataService.getInstallationCountFromFileOrInitializeRandomly(product.getId());
//
//    assertEquals(40, product.getInstallationCount());
//    assertTrue(product.getSynchronizedInstallationCount());
//  }
//}