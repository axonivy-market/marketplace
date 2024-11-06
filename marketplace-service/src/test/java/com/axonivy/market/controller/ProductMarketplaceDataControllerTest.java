package com.axonivy.market.controller;

import com.axonivy.market.enums.ErrorCode;
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

import static com.axonivy.market.constants.CommonConstants.AUTHORIZATION_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMarketplaceDataControllerTest {
  @Mock
  private ProductMarketplaceDataService productMarketplaceDataService;
  @InjectMocks
  private ProductMarketplaceDataController productMarketplaceDataController;

  @Test
  void testCreateCustomSortProductsSuccess() {
    ProductCustomSortRequest mockProductCustomSortRequest = createProductCustomSortRequestMock();
    var response = productMarketplaceDataController.createCustomSortProducts(AUTHORIZATION_HEADER,
        mockProductCustomSortRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
    assertEquals(ErrorCode.SUCCESSFUL.getCode(), Objects.requireNonNull(response.getBody()).getHelpCode());
    assertTrue(response.getBody().getMessageDetails().contains("Custom product sort order added successfully"));
  }

  @Test
  void testSyncInstallationCount() {
    when(
        productMarketplaceDataService.updateInstallationCountForProduct("google-maps-connector", "10.0.20")).thenReturn(
        1);
    var result = productMarketplaceDataController.syncInstallationCount("google-maps-connector", "10.0.20");

    assertEquals(1, result.getBody());
  }

  private ProductCustomSortRequest createProductCustomSortRequestMock() {
    List<String> productIds = new ArrayList<>();
    productIds.add("a-trust");
    productIds.add("approval-decision-utils");
    return new ProductCustomSortRequest(productIds, "recently");
  }
}