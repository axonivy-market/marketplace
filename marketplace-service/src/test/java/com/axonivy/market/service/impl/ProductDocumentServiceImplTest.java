package com.axonivy.market.service.impl;

import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDocumentServiceImplTest {

  @Mock
  ProductRepository productRepository;

  @InjectMocks
  ProductDocumentServiceImpl service;

  @Test
  void testSyncDocumentForProduct() {
    service.syncDocumentForProduct("portal", true);
  }
}
