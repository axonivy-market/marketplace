package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.VersionDownload;
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

import java.io.IOException;
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

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode());
    assertTrue(response.getBody().getMessageDetails().contains("Custom product sort order added successfully"));
  }

  @Test
  void testExtractArtifactUrl() throws IOException {
    when(productMarketplaceDataService.downloadArtifact(MOCK_DOWNLOAD_URL, MOCK_PRODUCT_ID)).thenReturn(
        new VersionDownload());
    var result = productMarketplaceDataController.extractArtifactUrl(MOCK_PRODUCT_ID, MOCK_DOWNLOAD_URL);

    assertNotNull(result);
  }

  @Test
  void testFindInstallationCount() {
    when(productMarketplaceDataService.getInstallationCount(MOCK_PRODUCT_ID)).thenReturn(5);
    var result = productMarketplaceDataController.findInstallationCount(MOCK_PRODUCT_ID);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result);
  }

  private ProductCustomSortRequest createProductCustomSortRequestMock() {
    List<String> productIds = new ArrayList<>();
    productIds.add("a-trust");
    productIds.add("approval-decision-utils");
    return new ProductCustomSortRequest(productIds, "recently");
  }
}