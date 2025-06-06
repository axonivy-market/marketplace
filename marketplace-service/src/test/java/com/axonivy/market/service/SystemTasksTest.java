package com.axonivy.market.service;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemTasksTest {

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepo;

  @Mock
  ExternalDocumentService externalDocumentService;

  @Mock
  ProductService productService;

  @Mock
  ProductDetailsController productDetailsController;

  @Mock
  ProductDependencyService productDependencyService;

  @InjectMocks
  ScheduledTasks tasks;

  @Test
  void testSyncDoc() {
    var mockProduct = Product.builder().id(PORTAL).build();
    when(productRepo.findAllProductsHaveDocument()).thenReturn(List.of(mockProduct));
    tasks.syncDataForProductDocuments();
    verify(productRepo, times(1)).findAllProductsHaveDocument();
    verify(externalDocumentService, times(1)).syncDocumentForProduct(PORTAL, false, null);
  }

  @Test
  void testSyncProduct() {
    tasks.syncDataForProductFromGitHubRepo();
    verify(productService, times(1)).syncLatestDataFromMarketRepo(false);
  }

  @Test
  void testSyncDataForProductMavenDependencies() {
    tasks.syncDataForProductMavenDependencies();
    verify(productDependencyService, times(1)).syncIARDependenciesForProducts(false, null);
  }

  @Test
  void testSyncDataForProductReleases() throws IOException {
    tasks.syncDataForProductReleases();
    verify(productDetailsController, atLeast(1)).syncLatestReleasesForProducts();
  }

  @Test
  void testFailedToSyncDataForProductReleasesWith() throws IOException {
    doThrow(new IOException()).when(productDetailsController).syncLatestReleasesForProducts();
    tasks.syncDataForProductReleases();

    assertThrows(IOException.class, () -> productDetailsController.syncLatestReleasesForProducts());
  }
}
