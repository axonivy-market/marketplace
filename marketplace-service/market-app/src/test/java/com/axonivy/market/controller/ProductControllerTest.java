package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest extends BaseSetup {
  private static final String PRODUCT_ID_SAMPLE = "a-trust";
  private static final String PRODUCT_PATH_SAMPLE = "market/connector/a-trust";
  private static final String PRODUCT_NAME_SAMPLE = "Amazon Comprehend";
  private static final String PRODUCT_NAME_DE_SAMPLE = "Amazon Comprehend DE";
  private static final String PRODUCT_DESC_SAMPLE = "Amazon Comprehend is a AI service that uses machine learning to " +
      "uncover information in unstructured data.";
  private static final String PRODUCT_DESC_DE_SAMPLE = "Amazon Comprehend is a AI service that uses machine learning " +
      "to uncover information in unstructured data. DE";

  @Mock
  private ProductService service;

  @Mock
  private ProductModelAssembler assembler;

  @Mock
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @InjectMocks
  private ProductController productController;

  @Mock
  private MetadataService metadataService;

  @Mock
  private GHAxonIvyMarketRepoService axonIvyMarketRepoService;

  @Mock
  private ProductDependencyService productDependencyService;

  @BeforeEach
  void setup() {
    assembler = new ProductModelAssembler();
  }

  @Test
  void testFindProductsAsEmpty() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<Product> mockProducts = new PageImpl<>(List.of(), pageable, 0);
    when(service.findProducts(any(), any(), any(), any(), any())).thenReturn(mockProducts);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());
    var result = productController.findProducts(TypeOption.ALL.getOption(), null, "en", false, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(result.hasBody(), "Expected response to have a body");
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size(),
        "Expected response body to contain 0 products");
  }

  @Test
  void testFindProducts() {
    PageRequest pageable = PageRequest.of(0, 20, Sort.by(Order.by(SortOption.ALPHABETICALLY.getOption())));
    Product mockProduct = createProductMock();

    Page<Product> mockProducts = new PageImpl<>(List.of(mockProduct), pageable, 1);
    when(service.findProducts(any(), any(), any(), any(), any())).thenReturn(mockProducts);
    assembler = new ProductModelAssembler();
    var mockProductModel = assembler.toModel(mockProduct);
    var mockPagedModel = PagedModel.of(List.of(mockProductModel), new PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(ProductModelAssembler.class))).thenReturn(mockPagedModel);
    var result = productController.findProducts(TypeOption.ALL.getOption(), "", "en", false, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(result.hasBody(), "Expected response to have a body");
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size(),
        "Expected response body size to be 1");
    assertEquals(PRODUCT_NAME_SAMPLE,
        result.getBody().getContent().iterator().next().getNames().get(Language.EN.getValue()),
        "Expected product English name to be " + PRODUCT_NAME_SAMPLE);
    assertEquals(PRODUCT_NAME_DE_SAMPLE,
        result.getBody().getContent().iterator().next().getNames().get(Language.DE.getValue()),
        "Expected product German name to be " + PRODUCT_NAME_DE_SAMPLE);
  }

  @Test
  void testSyncProductsSuccess() {
    when(service.syncLatestDataFromMarketRepo(false)).thenReturn(List.of());

    var response = productController.syncProducts(false);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(response.hasBody(),  "Expected response to have a body");
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode(),
        "Expected help code to be " + ErrorCode.SUCCESSFUL.getCode());
    assertEquals("Data is already up to date, nothing to sync", response.getBody().getMessageDetails(),
        "Expected message to be 'Data is already up to date, nothing to sync'");
  }

  @Test
  void testSyncProductsWithResetSuccess() {
    when(service.syncLatestDataFromMarketRepo(true)).thenReturn(List.of("portal"));

    var response = productController.syncProducts(true);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(response.hasBody(), "Expected response to have a body");
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode(),
        "Expected help code to be " + ErrorCode.SUCCESSFUL.getCode());
    assertTrue(response.getBody().getMessageDetails().contains("Finished sync [[portal]] data in"),
        "Expected message details to contain 'Finished sync [[portal]] data in'");
  }

  @Test
  void testSyncOneProductInvalidProductPath() {
    Product product = new Product();
    product.setId("a-trust");
    when(axonIvyMarketRepoService.getMarketItemByPath(any(String.class))).thenReturn(new ArrayList<>());
    var response = productController.syncOneProduct(PRODUCT_ID_SAMPLE,
        PRODUCT_PATH_SAMPLE, true);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
        "Expected HTTP status to be 200 OK when product path is invalid");
    assertTrue(response.hasBody(),
        "Response body should not be null or empty when product path is invalid");
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getHelpText(), response.getBody().getMessageDetails(),
        "Expected response messageDetails to indicate PRODUCT_NOT_FOUND");
  }

  @Test
  void testSyncOneProductSuccess() {
    Product product = new Product();
    product.setId("a-trust");
    GHContent content = mock(GHContent.class);
    List<GHContent> contents = new ArrayList<>();
    contents.add(content);
    when(axonIvyMarketRepoService.getMarketItemByPath(any(String.class))).thenReturn(contents);
    when(service.syncOneProduct(any(String.class), any(String.class), any(Boolean.class))).thenReturn(true);
    var response = productController.syncOneProduct(PRODUCT_ID_SAMPLE,
        PRODUCT_PATH_SAMPLE, true);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected HTTP status 200 OK when syncOneProduct succeeds");
    assertTrue(response.hasBody(),
        "Response body should not be null or empty when syncOneProduct succeeds");
    assertEquals("Sync successfully!", response.getBody().getMessageDetails(),
        "Expected success message 'Sync successfully!' in response body");
  }

  private Product createProductMock() {
    Product mockProduct = new Product();
    mockProduct.setId("amazon-comprehend");
    Map<String, String> name = new HashMap<>();
    name.put(Language.EN.getValue(), PRODUCT_NAME_SAMPLE);
    name.put(Language.DE.getValue(), PRODUCT_NAME_DE_SAMPLE);
    mockProduct.setNames(name);
    Map<String, String> shortDescription = new HashMap<>();
    shortDescription.put(Language.EN.getValue(), PRODUCT_DESC_SAMPLE);
    shortDescription.put(Language.DE.getValue(), PRODUCT_DESC_DE_SAMPLE);
    mockProduct.setShortDescriptions(shortDescription);
    mockProduct.setType("connector");
    mockProduct.setTags(List.of("AI"));
    return mockProduct;
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsFailed() {
    when(service.syncFirstPublishedDateOfAllProducts()).thenReturn(false);
    var response = productController.syncFirstPublishedDateOfAllProducts();

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
        "Response status should be OK even when syncing first published dates fails");
    assertNotEquals(ErrorCode.SUCCESSFUL.getCode(), response.getBody().getHelpCode(),
        "Help code should not indicate SUCCESSFUL when syncing first published dates fails");
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsSuccess() {
    when(service.syncFirstPublishedDateOfAllProducts()).thenReturn(true);
    var response = productController.syncFirstPublishedDateOfAllProducts();

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be OK when syncing first published dates succeeds");
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), response.getBody().getHelpCode(),
        "Help code should indicate SUCCESSFUL when syncing first published dates succeeds");
  }

  @Test
  void testSyncProductArtifactsSuccess() {
    when(productDependencyService.syncIARDependenciesForProducts(false, null)).thenReturn(5);

    var response = productController.syncProductArtifacts(false, null);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be OK when product artifacts are successfully synced");
    assertTrue(response.hasBody(),
        "Response should contain a body when product artifacts are successfully synced");
    assertEquals("Synced 5 artifact(s)", Objects.requireNonNull(response.getBody()).getMessageDetails(),
        "Response message should confirm that 5 artifacts were synced");
  }

  @Test
  void testSyncProductArtifactsNothingToSync() {
    when(productDependencyService.syncIARDependenciesForProducts(false, null)).thenReturn(0);

    var response = productController.syncProductArtifacts(false, null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
        "Response status should be NO_CONTENT when there are no artifacts to sync");
    assertTrue(response.hasBody(),
        "Response should still contain a body even when there are no artifacts to sync");
    assertEquals("Nothing to sync", Objects.requireNonNull(response.getBody()).getMessageDetails(),
        "Response message should indicate that there was nothing to sync");
  }
}
