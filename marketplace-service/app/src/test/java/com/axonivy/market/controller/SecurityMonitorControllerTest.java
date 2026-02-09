package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityMonitorControllerTest {

  @Mock
  private GitHubService gitHubService;

  @InjectMocks
  private SecurityMonitorController securityMonitorController;

  @Test
  void testGetGitHubMarketplaceSecurity() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    var product1 = new ProductSecurityInfo("product1", false, "public", true, new Date(), "abc123",
        null, null, null);
    var product2 = new ProductSecurityInfo("product2", false, "private", false, new Date(), "def456",
        null, null, null);
    List<ProductSecurityInfo> mockSecurityInfoList = Arrays.asList(product1, product2);
    when(gitHubService.getSecurityDetailsForAllProducts(null, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME)).thenReturn(mockSecurityInfoList);

    ResponseEntity<List<ProductSecurityInfo>> expectedResponse = new ResponseEntity<>(mockSecurityInfoList,
        HttpStatus.OK);

    ResponseEntity<Object> actualResponse = securityMonitorController.getGitHubMarketplaceSecurity(request);

    assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode(),
        "HTTP status codes do not match. Expected " + expectedResponse.getStatusCode() +
            " but got " + actualResponse.getStatusCode());
    assertEquals(expectedResponse.getBody(), actualResponse.getBody(),
        "Response body does not match expected security info list.");
  }
}
