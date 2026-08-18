package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.UpdateProductRequest;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHContent;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ProductController.class)
class ProductControllerTest extends WebMvcControllerTestSupport {

  private static final String PRODUCT_ID_SAMPLE = "a-trust";
  private static final String PRODUCT_PATH_SAMPLE = "market/connector/a-trust";

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @MockitoBean
  private ProductService service;

  @MockitoBean
  private ProductModelAssembler assembler;

  @MockitoBean
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @MockitoBean
  private GHAxonIvyMarketRepoService axonIvyMarketRepoService;

  @MockitoBean
  private ProductDependencyService productDependencyService;

  @MockitoBean
  private SyncTaskCancellationRegistry cancellationRegistry;

  @Test
  void testGetAllProductIds() throws Exception {
    List<String> productIds = List.of("a-trust", "amazon-comprehend");
    when(service.getProductIds()).thenReturn(productIds);

    mockMvc.perform(get("/api/product/ids"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("a-trust"))
        .andExpect(jsonPath("$[1]").value("amazon-comprehend"));
  }

  @Test
  void testSyncProductsSuccess() throws Exception {
    when(service.syncLatestDataFromMarketRepo(null)).thenReturn(List.of());

    mockMvc.perform(put("/api/product/sync")
            .with(requestedByHeader()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.helpCode").exists())
        .andExpect(jsonPath("$.messageDetails").value("Data is already up to date, nothing to sync"));
  }

  @Test
  void testSyncOneProductInvalidProductPath() throws Exception {
    when(axonIvyMarketRepoService.getMarketItemByPath(anyString())).thenReturn(List.of());

    mockMvc.perform(put("/api/product/sync/{id}", PRODUCT_ID_SAMPLE)
            .with(requestedByHeader())
            .param("marketItemPath", PRODUCT_PATH_SAMPLE)
            .param("overrideMarketItemPath", "true"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.messageDetails").value("PRODUCT_NOT_FOUND"));
  }

  @Test
  void testSyncOneProductSuccess() throws Exception {
    when(axonIvyMarketRepoService.getMarketItemByPath(anyString())).thenReturn(List.of(mock(GHContent.class)));
    when(service.syncOneProduct(anyString(), anyString(), any())).thenReturn(true);

    mockMvc.perform(put("/api/product/sync/{id}", PRODUCT_ID_SAMPLE)
            .with(requestedByHeader())
            .param("marketItemPath", PRODUCT_PATH_SAMPLE)
            .param("overrideMarketItemPath", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageDetails").value("Sync successfully!"));
  }

  @Test
  void testSyncProductArtifactsSuccess() throws Exception {
    when(productDependencyService.syncIARDependenciesForProducts(isNull(), isNull())).thenReturn(5);

    mockMvc.perform(put("/api/product/zip-sync")
            .with(requestedByHeader()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageDetails").value("Synced 5 artifact(s)"));
  }

  @Test
  void testUpdateProductSuccess() throws Exception {
    UpdateProductRequest request = new UpdateProductRequest(true);
    when(service.updateProduct(anyString(), any())).thenReturn(new Product());

    mockMvc.perform(put("/api/product/{id}", PRODUCT_ID_SAMPLE)
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageDetails").value("Product with id " + PRODUCT_ID_SAMPLE + " updated successfully"));
  }
}
