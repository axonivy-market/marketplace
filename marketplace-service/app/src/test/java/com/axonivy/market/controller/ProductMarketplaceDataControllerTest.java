package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.enums.RepositoryAction;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import com.axonivy.market.service.ProductMarketplaceDataService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ProductMarketplaceDataController.class)
class ProductMarketplaceDataControllerTest extends WebMvcControllerTestSupport {

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @MockitoBean
  private ProductMarketplaceDataService productMarketplaceDataService;

  @Test
  void testCreateCustomSortProducts() throws Exception {
    ProductCustomSortRequest request = createProductCustomSortRequestMock();

    mockMvc.perform(post("/api/product-marketplace-data/custom-sort")
            .with(requestedByHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageDetails").value("Custom product sort order added successfully"));
  }

  @Test
  void testGetCustomSortProducts() throws Exception {
    ProductCustomSortRequest request = createProductCustomSortRequestMock();
    when(productMarketplaceDataService.getCustomSortProducts()).thenReturn(request);

    mockMvc.perform(get("/api/product-marketplace-data/custom-sort"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderedListOfProducts[0]").value("a-trust"));
  }

  @Test
  void testFindInstallationCount() throws Exception {
    when(productMarketplaceDataService.getInstallationCount(MOCK_PRODUCT_ID)).thenReturn(5);

    mockMvc.perform(get("/api/product-marketplace-data/installation-count/{id}", MOCK_PRODUCT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(5));
  }

  @Test
  void testGetProductDeprecations() throws Exception {
    List<ProductDeprecationProjection> projections = List.of(
        createProductDeprecationProjection("a-trust", new Date()),
        createProductDeprecationProjection("amazon-comprehend", new Date())
    );
    when(productMarketplaceDataService.getProductIdsByDeprecated(null)).thenReturn(projections);

    mockMvc.perform(get("/api/product-marketplace-data/deprecations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..id").value(hasItem("a-trust")))
        .andExpect(jsonPath("$..id").value(hasItem("amazon-comprehend")));
  }

  @Test
  void testArchiveRepository() throws Exception {
    mockMvc.perform(put("/api/product-marketplace-data/{productId}/archive", "cms-live-editor")
            .with(requestedByHeader())
            .param("action", RepositoryAction.ARCHIVE.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageDetails").value("Repository archived successfully"));
  }

  private ProductCustomSortRequest createProductCustomSortRequestMock() {
    List<String> productIds = new ArrayList<>();
    productIds.add("a-trust");
    productIds.add("approval-decision-utils");
    return new ProductCustomSortRequest(productIds, "recently");
  }

  private ProductDeprecationProjection createProductDeprecationProjection(
      String id, Date deprecationDate) {
    return new ProductDeprecationProjection() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public Date getDeprecationDate() {
        return deprecationDate;
      }

      @Override
      public String getDeprecationRequester() {
        return "admin";
      }

      @Override
      public Boolean getDeprecated() {
        return null;
      }

      @Override
      public Boolean getIsArchived() {
        return null;
      }

    };
  }
}
