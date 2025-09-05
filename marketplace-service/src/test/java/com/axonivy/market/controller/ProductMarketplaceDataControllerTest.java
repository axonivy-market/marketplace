package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.service.ProductMarketplaceDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMarketplaceDataControllerTest extends BaseSetup {
  @Mock
  private ProductMarketplaceDataService productMarketplaceDataService;
  @Mock
  private GitHubService gitHubService;
  @InjectMocks
  private ProductMarketplaceDataController productMarketplaceDataController;

  @Test
  void testCreateCustomSortProducts() {
    ProductCustomSortRequest mockProductCustomSortRequest = createProductCustomSortRequestMock();
    var response = productMarketplaceDataController.createCustomSortProducts(AUTHORIZATION_HEADER,
        mockProductCustomSortRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Expected response status code: " + response.getStatusCode() + " to match HTTP status 200 OK");
    assertTrue(response.hasBody(), "Expected response to have a body");
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode(),
        "Expected response help code " + response.getBody().getHelpCode() +
            " to match " + ErrorCode.SUCCESSFUL.getCode());
    assertTrue(response.getBody().getMessageDetails().contains("Custom product sort order added successfully"));
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
    var result = productMarketplaceDataController.getArtifactResourceStream(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID,
        MOCK_DOWNLOAD_URL);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(),
        "Status code show return bad gateway when it can " + "not " +
            "forwarding the download stream");
    assertNull(result.getBody(), "Response body should not be null");
  }

  @Test
  void testFindInstallationCount() {
    when(productMarketplaceDataService.getInstallationCount(MOCK_PRODUCT_ID)).thenReturn(5);
    var result = productMarketplaceDataController.findInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Expected response status code: " + result.getStatusCode() + " to match HTTP status 200 OK");
    assertNotNull(result, "Response should not be null");
  }

  private ProductCustomSortRequest createProductCustomSortRequestMock() {
    List<String> productIds = new ArrayList<>();
    productIds.add("a-trust");
    productIds.add("approval-decision-utils");
    return new ProductCustomSortRequest(productIds, "recently");
  }
}