package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.controller.ProductController;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductModel;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.model.GitHubReleaseModel;

class ProductControllerDbTest {

  private Connection connection;
  private ProductController productController;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {
    connection = DriverManager.getConnection("jdbc:h2:mem:product-controller-db-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "sa", "password");

    createSchema(connection);
    seedData(connection);

    var productService = new H2BackedProductService(connection);

    var assembler = mock(ProductModelAssembler.class);
    when(assembler.toModel(any(Product.class))).thenAnswer(invocation -> {
      Product p = invocation.getArgument(0);
      var model = new ProductModel();
      model.setId(p.getId());
      model.setType(p.getType());
      model.setNames(p.getNames());
      return model;
    });

    var pagedAssembler = mock(PagedResourcesAssembler.class);
    when(pagedAssembler.toModel(any(Page.class), eq(assembler))).thenAnswer(invocation -> {
      Page<Product> page = invocation.getArgument(0);
      ProductModelAssembler asm = invocation.getArgument(1);
      List<ProductModel> models = page.stream().map(asm::toModel).toList();
      return PagedModel.of(models, new PagedModel.PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements()));
    });

    productController = new ProductController(
        productService,
        assembler,
        pagedAssembler,
        mock(GHAxonIvyMarketRepoService.class),
        mock(ProductDependencyService.class));
  }

  @AfterEach
  void tearDown() throws Exception {
    if (connection != null) {
      connection.close();
    }
  }

  @Test
  void shouldFindProductsFromInMemoryDatabase() {
    Pageable pageable = Pageable.ofSize(20).withPage(0);

    ResponseEntity<PagedModel<ProductModel>> response = productController.findProducts(
        "all",
        "portal",
        "en",
        false,
        pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getContent().size());

    ProductModel model = response.getBody().getContent().iterator().next();
    assertEquals("portal-db", model.getId());
    assertEquals("solution", model.getType());
    assertEquals("Portal DB", model.getNames().get("en"));
  }

  private static void createSchema(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE product (id VARCHAR(255) PRIMARY KEY, type VARCHAR(255), listed BOOLEAN)");
      statement.execute("CREATE TABLE product_name (product_id VARCHAR(255), language VARCHAR(10), name VARCHAR(1024), PRIMARY KEY(product_id, language))");
    }
  }

  private static void seedData(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("INSERT INTO product (id, type, listed) VALUES ('portal-db', 'solution', true)");
      statement.execute("INSERT INTO product_name (product_id, language, name) VALUES ('portal-db', 'en', 'Portal DB')");
    }
  }

  private record H2BackedProductService(Connection connection) implements ProductService {

    @Override
    public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
        Pageable pageable) {
      try (var ps = connection.prepareStatement("""
          SELECT p.id, p.type, n.name
          FROM product p
          JOIN product_name n ON n.product_id = p.id
          WHERE (p.listed <> FALSE OR p.listed IS NULL)
            AND LOWER(n.language) = LOWER(?)
            AND LOWER(n.name) LIKE ?
          ORDER BY p.id
          LIMIT ? OFFSET ?
          """)) {

        ps.setString(1, language);
        ps.setString(2, "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%");
        ps.setInt(3, pageable.getPageSize());
        ps.setLong(4, pageable.getOffset());

        try (var rs = ps.executeQuery()) {
          var products = new java.util.ArrayList<Product>();
          while (rs.next()) {
            var product = new Product();
            product.setId(rs.getString("id"));
            product.setType(rs.getString("type"));
            product.setNames(Map.of(language, rs.getString("name")));
            products.add(product);
          }
          return new PageImpl<>(products, pageable, products.size());
        }
      } catch (SQLException ex) {
        throw new IllegalStateException("Failed to query in-memory test database", ex);
      }
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
      throw new UnsupportedOperationException("Not used in this test");
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
