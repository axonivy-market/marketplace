package com.axonivy.market.neo.controller;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.MavenArtifactVersionModel;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.core.service.CoreProductService;
import com.axonivy.market.core.service.CoreVersionService;
import com.axonivy.market.neo.assembler.ProductModelAssembler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.neo.constants.RequestMappingConstants.PRODUCT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CoreVersionService versionService;

  @MockBean
  private CoreProductService coreProductService;

  @MockBean
  private ProductModelAssembler assembler;

  @MockBean
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  @Test
  void testFindProductJsonContent() throws Exception {
    when(versionService.getProductJsonContentByIdAndVersion(anyString(), anyString()))
        .thenReturn(Map.of("key", "value"));

    mockMvc.perform(get(PRODUCT + "/test-id/json")
            .param("designerVersion", "10.0.0"))
        .andExpect(status().isOk());
  }

  @Test
  void testFindProductVersionsById() throws Exception {
    when(versionService.getArtifactsAndVersionToDisplay(anyString(), anyBoolean(), anyString()))
        .thenReturn(List.of(new MavenArtifactVersionModel()));

    mockMvc.perform(get(PRODUCT + "/test-id/versions")
            .param("showDevVersion", "true")
            .param("designerVersion", "10.0.0"))
        .andExpect(status().isOk());
  }

  @Test
  void testFindProducts() throws Exception {
    Page<Product> productPage = new PageImpl<>(List.of(new Product()));
    when(coreProductService.findProducts(anyString(), anyString(), anyString(), any(), any(Pageable.class)))
        .thenReturn(productPage);
    when(pagedResourcesAssembler.toModel(any(Page.class), any(ProductModelAssembler.class)))
        .thenReturn(PagedModel.of(Collections.emptyList(), new PagedModel.PageMetadata(1, 0, 1)));

    mockMvc.perform(get(PRODUCT)
            .param("type", "all")
            .param("keyword", "test")
            .param("language", "en"))
        .andExpect(status().isOk());
  }

  @Test
  void testFindProductsEmpty() throws Exception {
    when(coreProductService.findProducts(anyString(), anyString(), anyString(), any(), any(Pageable.class)))
        .thenReturn(Page.empty());
    when(pagedResourcesAssembler.toEmptyModel(any(Page.class), any()))
        .thenReturn(PagedModel.of(Collections.emptyList(), new PagedModel.PageMetadata(0, 0, 0)));

    mockMvc.perform(get(PRODUCT))
        .andExpect(status().isOk());
  }
}
