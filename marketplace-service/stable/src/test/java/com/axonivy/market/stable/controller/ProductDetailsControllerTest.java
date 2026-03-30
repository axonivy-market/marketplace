package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.model.BestMatchVersion;
import com.axonivy.market.stable.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductDetailsControllerTest {

  @Mock
  private ProductServiceImpl productService;

  @InjectMocks
  private ProductDetailsController productDetailsController;
}
