package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

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

  @InjectMocks
  ScheduledTasks tasks;

  @Test
  void testSyncDoc() {
    var mockProduct = Product.builder().id(PORTAL).build();
    when(productRepo.findAllProductsHaveDocument()).thenReturn(List.of(mockProduct));
    tasks.syncDataForProductDocuments();
    verify(productRepo, times(1)).findAllProductsHaveDocument();
    verify(externalDocumentService, times(1)).syncDocumentForProduct(PORTAL, new ArrayList<>(), false);
  }

  @Test
  void testSyncProduct() {
    tasks.syncDataForProductFromGitHubRepo();
    verify(productService, times(1)).syncLatestDataFromMarketRepo(false);
  }
}
