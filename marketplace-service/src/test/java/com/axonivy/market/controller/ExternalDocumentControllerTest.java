package com.axonivy.market.controller;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.util.DocPathUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.axonivy.market.constants.RequestMappingConstants.ERROR_PAGE_404;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalDocumentControllerTest {

  private static final String TOKEN = "token";

  private static final String SAMPLE_PATH =
          "/portal/portal-guide/13.1.1/doc/_images/dashboard.png";
  private static final String SAMPLE_REDIRECT_PATH =
          "market-cache/portal/portal-guide/13.1.1/doc/_images/dashboard.png";

  private static final String VERSION = "13.1.1";
  private static final Path RESOLVE_PATH = Path.of(SAMPLE_PATH);
  private static final String PORTAL = "portal";

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ExternalDocumentService service;

  @InjectMocks
  private ExternalDocumentController externalDocumentController;


  @Test
  void testFindProductDoc() {
    when(service.findExternalDocument(any(), any())).thenReturn(createExternalDocumentMock());
    var result = externalDocumentController.findExternalDocument(PORTAL, VERSION);
    assertEquals(HttpStatus.OK, result.getStatusCode(), "Should be ok");
    assertTrue(result.hasBody(), "Should have body");
    assertTrue(ObjectUtils.isNotEmpty(result.getBody()), "Body should not be empty");
  }

  @Test
  void testRedirectToBestVersionSuccess() {
    when(service.resolveBestMatchRedirectUrl(SAMPLE_PATH))
        .thenReturn(SAMPLE_REDIRECT_PATH);

    try (MockedStatic<DocPathUtils> utilsMock = mockStatic(DocPathUtils.class)) {
      utilsMock.when(() -> DocPathUtils.resolveDocPath(anyString()))
          .thenReturn(RESOLVE_PATH);
      try (var filesMock = mockStatic(Files.class)) {
        filesMock.when(() -> Files.exists(RESOLVE_PATH)).thenReturn(true);
        ResponseEntity<Void> response = externalDocumentController.redirectToBestVersion(SAMPLE_PATH);
        assertTrue(response.getStatusCode().is3xxRedirection(), "Should be a redirection");
        assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString()
            .contains(SAMPLE_REDIRECT_PATH), "Should redirect to the correct path");
      }
    }
  }

  @Test
  void testRedirectToBestVersionWithInvalidPath() {
      ResponseEntity<Void> response = externalDocumentController.redirectToBestVersion(Strings.EMPTY);
      assertTrue(response.getStatusCode().is3xxRedirection(), "Should be a redirection");
      assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString()
          .contains(ERROR_PAGE_404), "Should redirect to 404");
  }

  @Test
  void testRedirectToBestVersionWithNoResource() {
    when(service.resolveBestMatchRedirectUrl(SAMPLE_PATH))
        .thenReturn(SAMPLE_REDIRECT_PATH);

    try (MockedStatic<DocPathUtils> utilsMock = mockStatic(DocPathUtils.class)) {
      utilsMock.when(() -> DocPathUtils.resolveDocPath(anyString()))
          .thenReturn(RESOLVE_PATH);
      try (var filesMock = mockStatic(Files.class)) {
        filesMock.when(() -> Files.exists(RESOLVE_PATH)).thenReturn(false);
        ResponseEntity<Void> response = externalDocumentController.redirectToBestVersion(SAMPLE_PATH);
        assertTrue(response.getStatusCode().is3xxRedirection(), "Should be a redirection");
        assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString()
            .contains(ERROR_PAGE_404), "Should redirect to 404");
      }
    }
  }

  @Test
  void testSyncDocumentForProduct() {
    var result = externalDocumentController.syncDocumentForProduct(TOKEN, true, null, null);
    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(), "Should be no product found");

    var mockProduct = mock(Product.class);
    when(service.findAllProductsHaveDocument()).thenReturn(List.of(mockProduct));
    result = externalDocumentController.syncDocumentForProduct(TOKEN, true, null, null);
    assertEquals(HttpStatus.OK, result.getStatusCode(), "Should return at least one product");
  }

  private ExternalDocumentMeta createExternalDocumentMock() {
    return ExternalDocumentMeta.builder()
        .relativeLink("/market-cache/portal/10.0.0/doc/index.html")
        .build();
  }
}