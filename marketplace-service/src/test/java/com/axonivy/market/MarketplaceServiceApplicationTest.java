package com.axonivy.market;

import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

class MarketplaceServiceApplicationTest {
  @Mock
  private ProductService productService;

  @Mock
  private ExternalDocumentService externalDocumentService;

  @InjectMocks
  private MarketplaceServiceApplication marketplaceServiceApplication;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSyncProductData_WhenNothingUpdated() {
    when(productService.syncLatestDataFromMarketRepo(false)).thenReturn(List.of());

    List<String> result = invokeSyncProductData();

    assertThat(result)
        .as("syncProductData should return an empty list when no product was updated")
        .isEmpty();
    verify(productService, times(1))
        .syncLatestDataFromMarketRepo(false);
  }

  @Test
  void testSyncProductData_WhenProductsUpdated() {
    List<String> products = List.of("p1", "p2");
    when(productService.syncLatestDataFromMarketRepo(false)).thenReturn(products);

    List<String> result = invokeSyncProductData();

    assertThat(result)
        .as("syncProductData should return the list of synced product IDs when update occurs")
        .containsExactlyInAnyOrder("p1", "p2");
    verify(productService, times(1))
        .syncLatestDataFromMarketRepo(false);
  }

  @Test
  void testSyncExternalDocumentData_WhenNoProductIds() {
    invokeSyncExternalDocumentData(List.of());
    verifyNoInteractions(externalDocumentService);
  }

  @Test
  void testSyncExternalDocumentData_WhenProductsExist() {
    List<String> productIds = List.of("p1", "p2");

    invokeSyncExternalDocumentData(productIds);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(externalDocumentService, times(2))
        .syncDocumentForProduct(captor.capture(), eq(false), isNull());

    assertThat(captor.getAllValues())
        .as("syncExternalDocumentData should call externalDocumentService with all product IDs")
        .containsExactlyInAnyOrder("p1", "p2");
  }

  @Test
  void testStartInitializeSystem_CallsBothSyncMethods() {
    List<String> productIds = List.of("p1", "p2");
    when(productService.syncLatestDataFromMarketRepo(false)).thenReturn(productIds);

    marketplaceServiceApplication.startInitializeSystem();

    verify(productService, times(1))
        .syncLatestDataFromMarketRepo(false);
    verify(externalDocumentService, times(2))
        .syncDocumentForProduct(anyString(), eq(false), isNull());
  }

  private List<String> invokeSyncProductData() {
    try {
      var method = MarketplaceServiceApplication.class.getDeclaredMethod("syncProductData");
      method.setAccessible(true);
      return (List<String>) method.invoke(marketplaceServiceApplication);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeSyncExternalDocumentData(List<String> productIds) {
    try {
      var method = MarketplaceServiceApplication.class.getDeclaredMethod("syncExternalDocumentData", List.class);
      method.setAccessible(true);
      method.invoke(marketplaceServiceApplication, productIds);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
