package com.axonivy.market.controller;

import com.axonivy.market.entity.Product;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.ExternalDocumentService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalDocumentControllerTest {

  private static final String TOKEN = "token";

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ExternalDocumentService service;

  @InjectMocks
  private ExternalDocumentController externalDocumentController;


  @Test
  void testFindProductDoc() throws URISyntaxException {
    when(service.findExternalDocumentURI(any(), any())).thenReturn("/market-cache/portal/10.0.0/doc/index.html");
    var result = externalDocumentController.findExternalDocumentURI("portal", "10.0");
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertTrue(ObjectUtils.isNotEmpty(result.getBody()));
  }

  @Test
  void testSyncDocumentForProduct() {
    var result = externalDocumentController.syncDocumentForProduct(TOKEN, true);
    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(), "Should be no product found");

    var mockProduct = mock(Product.class);
    when(service.findAllProductsHaveDocument()).thenReturn(List.of(mockProduct));
    result = externalDocumentController.syncDocumentForProduct(TOKEN, true);
    assertEquals(HttpStatus.OK, result.getStatusCode(), "Should return at least one product");
  }
}
