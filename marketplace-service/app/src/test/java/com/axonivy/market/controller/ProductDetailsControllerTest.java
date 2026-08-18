package com.axonivy.market.controller;

import com.axonivy.market.assembler.GithubReleaseModelAssembler;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.config.SyncTaskCancellationRegistry;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ProductDetailsController.class)
class ProductDetailsControllerTest extends WebMvcControllerTestSupport {

  private static final String DOCKER_CONNECTOR_ID = "docker-connector";

  @MockitoBean
  private ProductService productService;

  @MockitoBean
  private VersionService versionService;

  @MockitoBean
  private ProductDetailModelAssembler detailModelAssembler;

  @MockitoBean
  private PagedResourcesAssembler<GitHubReleaseModel> pagedResourcesAssembler;

  @MockitoBean
  private GithubReleaseModelAssembler githubReleaseModelAssembler;

  @MockitoBean
  private ProductContentService productContentService;

  @MockitoBean
  private SyncTaskCancellationRegistry cancellationRegistry;

  @Test
  void testProductDetailsByVersion() throws Exception {
    Product product = new Product();
    product.setId(DOCKER_CONNECTOR_ID);
    ProductDetailModel model = new ProductDetailModel();
    model.setId(DOCKER_CONNECTOR_ID);
    when(productService.fetchProductDetailByIdAndVersion(DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION))
        .thenReturn(product);
    when(detailModelAssembler.toModel(product)).thenReturn(model);

    mockMvc.perform(get("/api/product-details/{id}/{version}", DOCKER_CONNECTOR_ID, MOCK_RELEASED_VERSION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(DOCKER_CONNECTOR_ID));
  }

  @Test
  void testGetBestMatchVersion() throws Exception {
    when(productService.getBestMatchVersion(DOCKER_CONNECTOR_ID, "1.0.0", true)).thenReturn("1.0.0");

    mockMvc.perform(get("/api/product-details/{id}/{version}/best-match-version", DOCKER_CONNECTOR_ID, "1.0.0")
            .param("isShowDevVersion", "true"))
        .andExpect(status().isOk())
        .andExpect(content().string("1.0.0"));
  }

  @Test
  void testFindGithubPublicReleases() throws Exception {
    Page<GitHubReleaseModel> page = new PageImpl<>(List.of(new GitHubReleaseModel()));
    when(productService.getGitHubReleaseModels(anyString(), any())).thenReturn(page);
    when(pagedResourcesAssembler.toModel(any(Page.class), any(GithubReleaseModelAssembler.class)))
        .thenReturn(PagedModel.of(List.of(buildReleaseModel()), new PagedModel.PageMetadata(1, 0, 1)));

    mockMvc.perform(get("/api/product-details/{id}/releases", DOCKER_CONNECTOR_ID)
            .param("page", "0")
            .param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..name").exists());
  }

  @Test
  void testDownloadZipArtifactNotFound() throws Exception {
    when(productContentService.getDependencyUrls(MOCK_PRODUCT_ID, MOCK_DEMO_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/product-details/{id}/{artifactId}/{version}/zip-file",
            MOCK_PRODUCT_ID, MOCK_DEMO_ARTIFACT_ID, MOCK_RELEASED_VERSION))
        .andExpect(status().isNotFound());
  }

  @Test
  void testFindProductJsonContentByIdAndVersion() throws Exception {
    when(versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION, MOCK_DESIGNER_VERSION))
        .thenReturn(Map.of("version", MOCK_RELEASED_VERSION));

    mockMvc.perform(get("/api/product-details/{id}/{version}/json", MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)
            .param("designerVersion", MOCK_DESIGNER_VERSION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.version").value(MOCK_RELEASED_VERSION));
  }

  @Test
  void testGetLatestArtifactDownloadUrl() throws Exception {
    when(versionService.getLatestVersionArtifactDownloadUrl("portal", "1.0.0", "portal-app.zip"))
        .thenReturn("https://market.axonivy.com");

    mockMvc.perform(get("/api/product-details/{id}/artifact", "portal")
            .param("version", "1.0.0")
            .param("artifact", "portal-app.zip"))
        .andExpect(status().isOk())
        .andExpect(content().string("https://market.axonivy.com"));
  }

  private GitHubReleaseModel buildReleaseModel() {
    GitHubReleaseModel model = new GitHubReleaseModel();
    model.setName("1.0.0");
    return model;
  }
}
