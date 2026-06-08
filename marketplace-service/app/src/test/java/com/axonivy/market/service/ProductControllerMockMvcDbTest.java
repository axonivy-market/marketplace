package com.axonivy.market.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.controller.ProductController;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import com.axonivy.market.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.MapJoin;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = ProductControllerMockMvcDbTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:product-controller-db-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
class ProductControllerMockMvcDbTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductMarketplaceDataRepository productMarketplaceDataRepository;

  @MockBean
  private GHAxonIvyMarketRepoService axonIvyMarketRepoService;

  @MockBean
  private ProductDependencyService productDependencyService;

  @BeforeEach
  void setup() {
    productRepository.deleteAll();
    productMarketplaceDataRepository.deleteAll();

    var productId = "portal-db";
    var marketplaceData = ProductMarketplaceData.builder()
        .id(productId)
        .installationCount(7)
        .build();
    productMarketplaceDataRepository.save(marketplaceData);

    var product = Product.builder()
        .id(productId)
        .type("solution")
        .listed(true)
        .marketDirectory("market/solution/portal-db")
        .names(Map.of("en", "Portal DB"))
        .shortDescriptions(Map.of("en", "Portal from in-memory db"))
        .tags(List.of("database"))
        .releasedVersions(List.of("1.0.0"))
        .productMarketplaceData(marketplaceData)
        .build();

    productRepository.save(product);
  }

  @Test
  void shouldReturnProductsFromInMemoryDatabase() throws Exception {
    mockMvc.perform(get("/api/product")
            .param("type", "all")
            .param("keyword", "portal")
            .param("language", "en")
            .param("isRESTClient", "false")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.products[0].id").value("portal-db"))
        .andExpect(jsonPath("$._embedded.products[0].type").value("solution"))
        .andExpect(jsonPath("$._embedded.products[0].names.en").value("Portal DB"));
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @ComponentScan(basePackageClasses = {
      ProductController.class,
      ProductModelAssembler.class
  })
  @EntityScan(basePackageClasses = {
      Product.class,
      ProductMarketplaceData.class
  })
  @EnableJpaRepositories(basePackageClasses = {
      ProductRepository.class,
      ProductMarketplaceDataRepository.class,
      CoreProductRepository.class
  })
  static class TestApplication {

    @Bean
    @Primary
    ProductService productService(CoreProductRepository coreProductRepository) {
      return new DbBackedProductService(coreProductRepository);
    }
  }

  static class DbBackedProductService implements ProductService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CoreProductRepository coreProductRepository;

    DbBackedProductService(CoreProductRepository coreProductRepository) {
      this.coreProductRepository = coreProductRepository;
    }

    @Override
    public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
        Pageable pageable) {
      var cb = entityManager.getCriteriaBuilder();
      var query = cb.createQuery(Product.class);
      var root = query.from(Product.class);

      root.fetch("productMarketplaceData", JoinType.LEFT);
      MapJoin<Product, String, String> namesJoin = root.joinMap("names", JoinType.LEFT);
      namesJoin.on(cb.equal(namesJoin.key(), language));

      var listedPredicate = cb.or(cb.notEqual(root.get("listed"), false), cb.isNull(root.get("listed")));
      var keywordPattern = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
      var keywordPredicate = cb.like(cb.lower(namesJoin.value()), keywordPattern);

      query.select(root)
          .where(cb.and(listedPredicate, keywordPredicate))
          .groupBy(root, namesJoin.key(), namesJoin.value())
          .orderBy(cb.asc(root.get("id")));

      var typedQuery = entityManager.createQuery(query)
          .setFirstResult((int) pageable.getOffset())
          .setMaxResults(pageable.getPageSize());

      var content = typedQuery.getResultList();
      return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public List<String> syncLatestDataFromMarketRepo(Boolean resetSync) {
      return Collections.emptyList();
    }

    @Override
    public Product fetchProductDetail(String id, Boolean isShowDevVersion) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Product fetchBestMatchProductDetail(String id, String version) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Product fetchProductDetailByIdAndVersion(String id, String version) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public boolean syncFirstPublishedDateOfAllProducts() {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Page<GitHubReleaseModel> getGitHubReleaseModels(String productId, Pageable pageable) throws IOException {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Page<GitHubReleaseModel> syncGitHubReleaseModels(String productId, Pageable pageable) throws IOException {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(String productId, Long releaseId)
        throws IOException {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public List<String> getProductIds() {
      return coreProductRepository.findAll().stream().map(Product::getId).toList();
    }

    @Override
    public Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public String getBestMatchVersion(String productId, String version, Boolean isShowDevVersion) {
      throw new UnsupportedOperationException("Not used in this test");
    }
  }
}
