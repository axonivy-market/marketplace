package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import com.axonivy.market.service.ProductMarketplaceDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMarketplaceDataControllerTest extends BaseSetup {
  @Mock
  private ProductMarketplaceDataService productMarketplaceDataService;
  @InjectMocks
  private ProductMarketplaceDataController productMarketplaceDataController;

  @Test
  void testCreateCustomSortProducts() {
    ProductCustomSortRequest mockProductCustomSortRequest = createProductCustomSortRequestMock();
    var response = productMarketplaceDataController.createCustomSortProducts(mockProductCustomSortRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected response status code: " + response.getStatusCode() + " to match HTTP status 200 OK");
    assertTrue(response.hasBody(), "Expected response to have a body");
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode(),
        "Expected response help code " + response.getBody().getHelpCode() +
            " to match " + ErrorCode.SUCCESSFUL.getCode());
    assertTrue(response.getBody().getMessageDetails().contains("Custom product sort order added successfully"),
        "Response body message details should contain 'Custom product sort order added successfully'");
  }

  @Test
  void testGetCustomSortProducts() {
    ProductCustomSortRequest mockProductCustomSortRequest = createProductCustomSortRequestMock();
    when(productMarketplaceDataService.getCustomSortProducts()).thenReturn(mockProductCustomSortRequest);

    var response = productMarketplaceDataController.getCustomSortProducts();

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Status code should be 200 when fetching custom sort products");

    assertEquals(mockProductCustomSortRequest, response.getBody(),
        "Response body should match the custom sort products returned by the service");
  }

  @Test
  void testExtractArtifactUrl() {
    when(productMarketplaceDataService.getProductArtifactStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION)).thenReturn(getMockEntityResource());
    var result = productMarketplaceDataController.getArtifactResourceStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION);
    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Expected response status code: " + result.getStatusCode() + " to match HTTP status 200 OK");
    assertNotNull(result, "Response should not be null");
  }

  @Test
  void testExtractArtifactUrlReturnBadGateWay() {
    when(productMarketplaceDataService.getProductArtifactStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_DOWNLOAD_URL))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null));

    assertThrows(NotFoundException.class,
        () -> productMarketplaceDataController.getArtifactResourceStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
            MOCK_DOWNLOAD_URL), "Expected NotFoundException to be thrown when artifact resource is not found");
  }

  @Test
  void testGetArtifactResourceStreamWhenServiceReturnsNullShouldThrowNotFoundException() {
    when(productMarketplaceDataService.getProductArtifactStream(
        MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .thenReturn(null);

    assertThrows(NotFoundException.class,
        () -> productMarketplaceDataController.getArtifactResourceStream(
            MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION),
        "Expected NotFoundException when service returns null ResponseEntity");
  }

  @Test
  void testFindInstallationCount() {
    when(productMarketplaceDataService.getInstallationCount(MOCK_PRODUCT_ID)).thenReturn(5);
    var result = productMarketplaceDataController.findInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Expected response status code: " + result.getStatusCode() + " to match HTTP status 200 OK");
    assertNotNull(result, "Response should not be null");
  }

  @Test
  void testGetProductDeprecations() {
    List<ProductDeprecationProjection> projections = List.of(
        createProductDeprecationProjection("a-trust", new Date()),
        createProductDeprecationProjection("amazon-comprehend", new Date())
    );
    when(productMarketplaceDataService.getProductIdsByDeprecated(null)).thenReturn(projections);

    var response = productMarketplaceDataController.getProductDeprecations(null);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(response.hasBody(), "Response body should not be null");
    assertEquals(2, Objects.requireNonNull(response.getBody()).size(),
        "Expected response to contain 2 deprecation projections");
  }

  @Test
  void testUpdateDeprecatedMarketplaceData() throws Exception {
    String productId = "cms-live-editor";
    DeprecationRequest request = new DeprecationRequest();
    request.setIsDeprecated(true);
    request.setSuccessorUrl("https://example.com/successor");
    request.setIsAddReadme(false);
    request.setPullRequestAction(PullRequestAction.ADD);

    when(productMarketplaceDataService.updateSuccessorForProduct(productId, request))
        .thenReturn("https://github.com/org/repo/pull/123");

    ResponseEntity<String> response =
        productMarketplaceDataController.updateDeprecatedMarketplaceData(request, productId);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
    assertTrue(response.hasBody(), "Response body should not be null");
    assertEquals("https://github.com/org/repo/pull/123", response.getBody(),
        "Response body should match service result");
    verify(productMarketplaceDataService).updateSuccessorForProduct(productId, request);
  }

  @Test
  void testUpdateDeprecatedMarketplaceDataThrowsIOException() throws Exception {
    String productId = "cms-live-editor";
    DeprecationRequest request = new DeprecationRequest();
    when(productMarketplaceDataService.updateSuccessorForProduct(productId, request))
        .thenThrow(new IOException("mock IO error"));

    assertThrows(IOException.class,
        () -> productMarketplaceDataController.updateDeprecatedMarketplaceData(request, productId),
        "Expected IOException to propagate from service");

    verify(productMarketplaceDataService).updateSuccessorForProduct(productId, request);
  }

  private ProductCustomSortRequest createProductCustomSortRequestMock() {
    List<String> productIds = new ArrayList<>();
    productIds.add("a-trust");
    productIds.add("approval-decision-utils");
    return new ProductCustomSortRequest(productIds, "recently");
  }

  private ProductDeprecationProjection createProductDeprecationProjection(
      String id, Date deprecationDate) {
    return new ProductDeprecationProjection() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public Date getDeprecationDate() {
        return deprecationDate;
      }

      @Override
      public String getDeprecationRequester() {
        return "admin";
      }

      @Override
      public Boolean getDeprecated() {
        return null;
      }

      @Override
      public Boolean getIsArchived() {
        return null;
      }

    };
  }
}
