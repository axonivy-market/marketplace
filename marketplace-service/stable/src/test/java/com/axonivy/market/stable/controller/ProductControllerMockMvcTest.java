package com.axonivy.market.stable.controller;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.core.service.CoreProductService;
import com.axonivy.market.stable.assembler.ProductModelAssembler;
import com.axonivy.market.stable.service.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

  @Mock
  private CoreProductService coreProductService;

  @Mock
  private ProductModelAssembler assembler;

  @Captor
  private ArgumentCaptor<Pageable> pageableCaptor;

  @BeforeEach
  void setUp() {
    ProductController productController = new ProductController(versionService, coreProductService, assembler);
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

  @Test
  void shouldReturnProductVersionsWithRequestParamsBound() throws Exception {
    when(versionService.getArtifactsAndVersionToDisplay("connectivity-demo", true, "10.0.20"))
        .thenReturn(List.of(new MavenArtifactVersionModel("10.0.20", List.of())));

    mockMvc.perform(get("/api/product/connectivity-demo/versions")
            .param("isShowDevVersion", "true")
            .param("designerVersion", "10.0.20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].version").value("10.0.20"))
        .andExpect(jsonPath("$[0].artifactsByVersion").isArray());

    verify(versionService).getArtifactsAndVersionToDisplay("connectivity-demo", true, "10.0.20");
  }

  @Test
  void shouldReturnEmptyVersionListWhenNoArtifactIsAvailable() throws Exception {
    when(versionService.getArtifactsAndVersionToDisplay("connectivity-demo", false, "10.0.20"))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/product/connectivity-demo/versions")
            .param("isShowDevVersion", "false")
            .param("designerVersion", "10.0.20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(versionService).getArtifactsAndVersionToDisplay("connectivity-demo", false, "10.0.20");
  }

  @Test
  void shouldFallBackToFalseWhenShowDevVersionParamIsOmitted() throws Exception {
    when(versionService.getArtifactsAndVersionToDisplay("connectivity-demo", false, null))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/product/connectivity-demo/versions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(versionService).getArtifactsAndVersionToDisplay("connectivity-demo", false, null);
  }

  @Test
  void shouldReturnProductsFilteredByRequestParams() throws Exception {
    Product product = new Product();
    product.setId("connectivity-demo");
    Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, Integer.MAX_VALUE), 1);
    when(coreProductService.findProducts("connectors", "connectivity", "en", PageRequest.of(0, Integer.MAX_VALUE)))
        .thenReturn(productPage);

    ProductModel productModel = new ProductModel();
    productModel.setId("connectivity-demo");
    productModel.setNames(Map.of("en", "Connectivity Demo"));
    when(assembler.toModel(product)).thenReturn(productModel);

    mockMvc.perform(get("/api/product")
            .param("type", "connectors")
            .param("keyword", "connectivity")
            .param("language", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value("connectivity-demo"))
        .andExpect(jsonPath("$[0].names.en").value("Connectivity Demo"));

    verify(assembler).toModel(product);
  }

  @Test
  void shouldRequestAllProductsOnASinglePage() throws Exception {
    when(coreProductService.findProducts(any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

    mockMvc.perform(get("/api/product")).andExpect(status().isOk());

    verify(coreProductService).findProducts(any(), any(), any(), pageableCaptor.capture());
    assertEquals(0, pageableCaptor.getValue().getPageNumber(), "Expected the first page to be requested");
    assertEquals(Integer.MAX_VALUE, pageableCaptor.getValue().getPageSize(),
        "Expected all products to be fetched in one page");
  }

  @Test
  void shouldPassNullFiltersWhenNoRequestParamIsGiven() throws Exception {
    when(coreProductService.findProducts(null, null, null, PageRequest.of(0, Integer.MAX_VALUE)))
        .thenReturn(Page.empty());

    mockMvc.perform(get("/api/product"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(coreProductService).findProducts(null, null, null, PageRequest.of(0, Integer.MAX_VALUE));
  }

  @Test
  void shouldReturnEmptyListWhenNoProductMatchesTheFilters() throws Exception {
    when(coreProductService.findProducts("connectors", "unknown", "en", PageRequest.of(0, Integer.MAX_VALUE)))
        .thenReturn(Page.empty());

    mockMvc.perform(get("/api/product")
            .param("type", "connectors")
            .param("keyword", "unknown")
            .param("language", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(assembler, never()).toModel(any(Product.class));
  }
}
