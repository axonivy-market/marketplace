package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerMockMvcTest {

  private MockMvc mockMvc;

  @Mock
  private ProductService productService;

  @BeforeEach
  void setUp() {
    ProductDetailsController productDetailsController = new ProductDetailsController(productService);
    mockMvc = MockMvcBuilders.standaloneSetup(productDetailsController).build();
  }

  @Test
  void shouldReturnBestMatchVersionWithDefaultShowDevVersion() throws Exception {
    when(productService.getBestMatchVersion("approval-decision-utils", "10.0.20", false))
        .thenReturn("10.0.19");

    mockMvc.perform(get("/api/product-details/approval-decision-utils/10.0.20/bestmatch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("10.0.19"));

    verify(productService).getBestMatchVersion("approval-decision-utils", "10.0.20", false);
  }

  @Test
  void shouldPassShowDevVersionParamToService() throws Exception {
    when(productService.getBestMatchVersion("approval-decision-utils", "10.0.20", true))
        .thenReturn("10.0.21-SNAPSHOT");

    mockMvc.perform(get("/api/product-details/approval-decision-utils/10.0.20/bestmatch")
            .param("isShowDevVersion", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("10.0.21-SNAPSHOT"));

    verify(productService).getBestMatchVersion("approval-decision-utils", "10.0.20", true);
  }

  @Test
  void shouldPassShowDevVersionFalseWhenExplicitlyRequested() throws Exception {
    when(productService.getBestMatchVersion("approval-decision-utils", "10.0.20", false))
        .thenReturn("10.0.19");

    mockMvc.perform(get("/api/product-details/approval-decision-utils/10.0.20/bestmatch")
            .param("isShowDevVersion", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value("10.0.19"));

    verify(productService).getBestMatchVersion("approval-decision-utils", "10.0.20", false);
  }

  @Test
  void shouldReturnNullVersionWhenNoBestMatchFound() throws Exception {
    when(productService.getBestMatchVersion("approval-decision-utils", "99.0.0", false))
        .thenReturn(null);

    mockMvc.perform(get("/api/product-details/approval-decision-utils/99.0.0/bestmatch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(nullValue()));

    verify(productService).getBestMatchVersion("approval-decision-utils", "99.0.0", false);
  }
}
