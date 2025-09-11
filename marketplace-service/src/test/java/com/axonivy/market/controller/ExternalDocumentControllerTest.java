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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalDocumentControllerTest {

  private static final String TOKEN = "token";

  private static final String SAMPLE_PATH =
          "/portal/portal-guide/13.1.1/doc/_images/dashboard.png";
  private static final String SAMPLE_REDIRECT_PATH =
          "market-cache/portal/portal-guide/13.1.1/doc/_images/dashboard.png";

  @Mock
  private GitHubService gitHubService;

  @Mock
  private ExternalDocumentService service;

  @InjectMocks
  private ExternalDocumentController externalDocumentController;


  @Test
  void testFindProductDoc() {
    when(service.findExternalDocument(any(), any())).thenReturn(createExternalDocumentMock());
    var result = externalDocumentController.findExternalDocument("portal", "10.0");
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertTrue(ObjectUtils.isNotEmpty(result.getBody()));
  }

  @Test
  void testRedirectToBestVersion() {
      // arrange
      String requestedVersion = "13.1.1";
      String bestMatchVersion = "15.0";
      Path mockResolvedPath = Path.of(SAMPLE_PATH);
      when(service.findBestMatchVersion("portal", requestedVersion))
              .thenReturn(bestMatchVersion);

      try (MockedStatic<DocPathUtils> utilsMock = mockStatic(DocPathUtils.class);
           MockedStatic<Files> filesMock = mockStatic(Files.class)) {

          utilsMock.when(() -> DocPathUtils.extractVersion(anyString()))
                  .thenReturn(requestedVersion);
          utilsMock.when(() -> DocPathUtils.extractProductId(anyString()))
                  .thenReturn("portal");
          utilsMock.when(() -> DocPathUtils.updateVersionInPath(anyString(), anyString(), anyString()))
                  .thenReturn(SAMPLE_PATH);
          utilsMock.when(() -> DocPathUtils.resolveDocPath(anyString()))
                  .thenReturn(mockResolvedPath);
          filesMock.when(() -> Files.exists(mockResolvedPath)).thenReturn(true);

          ResponseEntity<Void> response = externalDocumentController.redirectToBestVersion(SAMPLE_PATH);

          assertTrue(response.getStatusCode().is3xxRedirection(), "Should be a redirection");
          assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString()
                  .contains(SAMPLE_REDIRECT_PATH), "Should redirect to the correct path");
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
