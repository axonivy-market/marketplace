package com.axonivy.market.stable.controller;

import com.axonivy.market.stable.service.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerMockMvcTest {

  private MockMvc mockMvc;

  @Mock
  private VersionService versionService;

  @BeforeEach
  void setUp() {
    ProductController productController = new ProductController(versionService, null, null, null);
    mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
  }

  @Test
  void shouldReturnProductJsonContentForInstallRequest() throws Exception {
    when(versionService.getProductJsonContentByIdAndVersion("connectivity-demo", "13.2.0"))
        .thenReturn(Map.of("id", "connectivity-demo"));

    mockMvc.perform(get("/api/product/connectivity-demo/install")
            .param("productVersion", "13.2.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("connectivity-demo"));

    verify(versionService).getProductJsonContentByIdAndVersion("connectivity-demo", "13.2.0");
  }

  @Test
  void shouldReturnNotFoundWhenProductJsonContentIsMissing() throws Exception {
    when(versionService.getProductJsonContentByIdAndVersion("connectivity-demo", null))
        .thenReturn(Map.of());

    mockMvc.perform(get("/api/product/connectivity-demo/install"))
        .andExpect(status().isNotFound());

    verify(versionService).getProductJsonContentByIdAndVersion("connectivity-demo", null);
  }
}