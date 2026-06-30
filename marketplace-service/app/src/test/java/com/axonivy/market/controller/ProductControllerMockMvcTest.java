package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.service.AppSettingService;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ProductDependencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(com.axonivy.market.controller.ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProductControllerMockMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private com.axonivy.market.service.ProductService productService;

  @MockitoBean
  private ExternalDocumentService externalDocumentService;

  @MockitoBean
  private GitHubService gitHubService;

  @MockitoBean
  private ProductModelAssembler assembler;

  @MockitoBean
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @MockitoBean
  private AppSettingService appSettingService;

  @MockitoBean
  private GHAxonIvyMarketRepoService axonIvyMarketRepoService;

  @MockitoBean
  private ProductDependencyService productDependencyService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @MockitoBean
  private CacheManager cacheManager;

  @Test
  void shouldReturnProductsForSearchRequest() throws Exception {
    Product product = new Product();
    product.setId("portal");
    product.setType("solution");
    product.setNames(Map.of("en", "Portal"));

    Pageable pageable = PageRequest.of(0, 20);
    when(productService.findProducts(eq("all"), eq("portal"), eq("en"), eq(false), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

    ProductModel productModel = new ProductModel();
    productModel.setId("portal");
    productModel.setType("solution");
    productModel.setNames(Map.of("en", "Portal"));

    PagedModel<ProductModel> pagedModel = PagedModel.of(
        List.of(productModel),
        new PagedModel.PageMetadata(20, 0, 1)
    );
    when(pagedResourcesAssembler.toModel(org.mockito.ArgumentMatchers.<Page<Product>>any(), eq(assembler)))
      .thenReturn(pagedModel);

    mockMvc.perform(get("/api/product")
            .param("type", "all")
            .param("keyword", "portal")
            .param("language", "en")
            .param("isRESTClient", "false")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..id").value(hasItem("portal")))
        .andExpect(jsonPath("$..type").value(hasItem("solution")))
        .andExpect(jsonPath("$..names.en").value(hasItem("Portal")));
  }
}
