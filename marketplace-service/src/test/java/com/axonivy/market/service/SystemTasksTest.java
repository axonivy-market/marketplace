package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemTasksTest {

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepo;

  @Mock
  ProductDocumentService productDocumentService;

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
    verify(productDocumentService, times(1)).syncDocumentForProduct(PORTAL, false);
  }

  @Test
  void testSyncProduct() {
    tasks.syncDataForProductFromGitHubRepo();
    verify(productService, times(1)).syncLatestDataFromMarketRepo();
  }
}
