package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.util.validator.AuthorizationUtils;
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
  private static final String INVALID_AUTHORIZATION_HEADER = "Bearer invalid_token";

  @Mock
  private ProductService service;

  @Mock
  private ProductModelAssembler assembler;

  @Mock
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @Mock
  private GitHubService gitHubService;

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
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size());
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
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size());
    assertEquals(PRODUCT_NAME_SAMPLE,
        result.getBody().getContent().iterator().next().getNames().get(Language.EN.getValue()));
    assertEquals(PRODUCT_NAME_DE_SAMPLE,
        result.getBody().getContent().iterator().next().getNames().get(Language.DE.getValue()));
  }

  @Test
  void testSyncProductsSuccess() {
    when(service.syncLatestDataFromMarketRepo(false)).thenReturn(List.of());

    var response = productController.syncProducts(AUTHORIZATION_HEADER, false);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode());
    assertEquals("Data is already up to date, nothing to sync", response.getBody().getMessageDetails());
  }

  @Test
  void testSyncProductsWithResetSuccess() {
    when(service.syncLatestDataFromMarketRepo(true)).thenReturn(List.of("portal"));

    var response = productController.syncProducts(AUTHORIZATION_HEADER, true);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode());
    assertTrue(response.getBody().getMessageDetails().contains("Finished sync [[portal]] data in"));
  }

  @Test
  void testSyncProductsInvalidToken() {
    doThrow(new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText())).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(String.class), any(String.class), any(String.class));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> productController.syncProducts(INVALID_AUTHORIZATION_HEADER, false));

    assertEquals(ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), exception.getMessage());
  }

  @Test
  void testSyncOneProductInvalidProductPath() {
    Product product = new Product();
    product.setId("a-trust");
    when(axonIvyMarketRepoService.getMarketItemByPath(any(String.class))).thenReturn(new ArrayList<>());
    var response = productController.syncOneProduct(AUTHORIZATION_HEADER, PRODUCT_ID_SAMPLE,
        PRODUCT_PATH_SAMPLE, true);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getHelpText(), response.getBody().getMessageDetails());
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
    var response = productController.syncOneProduct(AUTHORIZATION_HEADER, PRODUCT_ID_SAMPLE,
        PRODUCT_PATH_SAMPLE, true);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals("Sync successfully!", response.getBody().getMessageDetails());
  }

  @Test
  void testSyncOneProductInvalidToken() {
    doThrow(new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText())).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(String.class), any(String.class), any(String.class));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> productController.syncOneProduct(INVALID_AUTHORIZATION_HEADER, PRODUCT_ID_SAMPLE,
            PRODUCT_PATH_SAMPLE, false));

    assertEquals(ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), exception.getMessage());
  }

  @Test
  void testGetBearerTokenWithValidHeader() {
    String token = AuthorizationUtils.getBearerToken(AUTHORIZATION_HEADER);
    assertEquals("valid_token", token);
  }

  @Test
  void testGetBearerTokenWithInvalidHeader() {
    String token = AuthorizationUtils.getBearerToken("InvalidTokenFormat");
    assertNull(token);
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
  void testSyncFirstPublishedDateOfAllProductsInvalidToken() {
    doThrow(new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText())).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(String.class), any(String.class), any(String.class));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> productController.syncFirstPublishedDateOfAllProducts(INVALID_AUTHORIZATION_HEADER));

    assertEquals(ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), exception.getMessage());
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsFailed() {
    when(service.syncFirstPublishedDateOfAllProducts()).thenReturn(false);
    var response = productController.syncFirstPublishedDateOfAllProducts(AUTHORIZATION_HEADER);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotEquals(ErrorCode.SUCCESSFUL.getCode(), response.getBody().getHelpCode());
  }

  @Test
  void testSyncFirstPublishedDateOfAllProductsSuccess() {
    when(service.syncFirstPublishedDateOfAllProducts()).thenReturn(true);
    var response = productController.syncFirstPublishedDateOfAllProducts(AUTHORIZATION_HEADER);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), response.getBody().getHelpCode());
  }

  @Test
  void testSyncProductArtifactsSuccess() {
    when(productDependencyService.syncIARDependenciesForProducts(false, null)).thenReturn(5);

    var response = productController.syncProductArtifacts(AUTHORIZATION_HEADER, false, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals("Synced 5 artifact(s)", Objects.requireNonNull(response.getBody()).getMessageDetails());
  }

  @Test
  void testSyncProductArtifactsNothingToSync() {
    when(productDependencyService.syncIARDependenciesForProducts(false, null)).thenReturn(0);

    var response = productController.syncProductArtifacts(AUTHORIZATION_HEADER, false, null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals("Nothing to sync", Objects.requireNonNull(response.getBody()).getMessageDetails());
  }

  @Test
  void testSyncProductArtifactsInvalidToken() {
    doThrow(new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText())).when(gitHubService)
        .validateUserInOrganizationAndTeam(any(String.class), any(String.class), any(String.class));

    UnauthorizedException exception = assertThrows(UnauthorizedException.class,
        () -> productController.syncProductArtifacts(INVALID_AUTHORIZATION_HEADER, false, null));

    assertEquals(ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), exception.getMessage());
  }
}
