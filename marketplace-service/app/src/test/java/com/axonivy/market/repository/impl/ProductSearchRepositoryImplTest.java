package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.MarketplaceServiceApplication;
import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.entity.ProductModuleContent;
import com.axonivy.market.core.enums.DocumentField;
import com.axonivy.market.core.enums.SortOption;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.ProductMarketplaceDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MarketplaceServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class ProductSearchRepositoryImplTest extends BaseSetup {
  private static final String LISTED_PRODUCT_ID = "case-process-viewer-utils";
  private static final String DOCUMENTED_PRODUCT_ID = "express-importer";
  private static final String PORTAL_PRODUCT_ID = "portal";
  private static final String LISTED_PRODUCT_NAME = "Case Process Viewer";
  private static final String LISTED_PRODUCT_VERSION = "13.2.3";

  @Autowired
  private ProductRepository repository;
  @Autowired
  private ProductMarketplaceDataRepository productMarketplaceDataRepository;

  @Test
  void testFindAllProductsHaveDocument() {
    List<Product> products = repository.findAllProductsHaveDocument();

    assertThat(products)
        .as("documents should only exist for the expected products")
        .extracting(Product::getId)
        .containsExactlyInAnyOrder(PORTAL_PRODUCT_ID);
  }

  @Test
  void testFindProductByKeywordInName() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setKeyword(LISTED_PRODUCT_NAME);

    Product product = repository.findByCriteria(criteria);

    assertThat(product)
        .as("keyword search should return the matching product")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the keyword result")
        .isEqualTo(LISTED_PRODUCT_ID);
  }

  @Test
  void testFindProductByMarketDirectoryWhenFieldIsRestricted() {
    ProductSearchCriteria criteria = new ProductSearchCriteria();
    criteria.setFields(List.of(DocumentField.MARKET_DIRECTORY));
    criteria.setKeyword("market/utils/express-importer/");

    Product product = repository.findByCriteria(criteria);

    assertThat(product)
        .as("restricted field search should still return the matching product")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the market directory lookup")
        .isEqualTo(DOCUMENTED_PRODUCT_ID);
  }

  @Test
  void testAttachModuleContentForRequestedVersion() {
    Product product = repository.getProductByIdAndVersion(LISTED_PRODUCT_ID, LISTED_PRODUCT_VERSION);

    assertThat(product)
        .as("product should be returned for the requested version")
        .isNotNull();
    assertThat(product.getId())
        .as("returned product id should match the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);

    ProductModuleContent content = product.getProductModuleContent();
    assertThat(content)
        .as("module content should be attached for the requested version")
        .isNotNull();
    assertThat(content.getProductId())
        .as("module content should belong to the requested product")
        .isEqualTo(LISTED_PRODUCT_ID);
    assertThat(content.getVersion())
        .as("module content version should match the request")
        .isEqualTo(LISTED_PRODUCT_VERSION);
    assertThat(content.getDescription())
        .as("description should include the English entry")
        .containsEntry("en",
        "This Axon Ivy component visually represents the process flow of your current case. It highlights both the active task and all completed tasks directly on the process diagram.");
    assertThat(content.getSetup())
        .as("setup should include the English entry")
        .containsEntry("en", "Add the Component to Your JSF Page");
    assertThat(content.getDemo())
        .as("demo should include the English entry")
        .containsEntry("en", "1. Start **Purchase Request Demo** process");
    assertThat(content.getComponent())
        .as("component should include the English entry")
        .containsEntry("en", "");
  }

  @Test
  void testStandardSortKeepsCustomOrderedProductsFirstAndAlphabeticalNullRemainder() {
    Product customHigh = createProduct("standard-sort-custom-high", "Zulu Custom");
    Product customLow = createProduct("standard-sort-custom-low", "Alpha Custom");
    Product nullAlpha = createProduct("standard-sort-null-alpha", "Alpha Null");
    Product nullZulu = createProduct("standard-sort-null-zulu", "Zulu Null");

    productMarketplaceDataRepository.resetCustomOrderForAllProducts();
    repository.saveAll(List.of(customHigh, customLow, nullAlpha, nullZulu));
    productMarketplaceDataRepository.saveAll(List.of(
        createMarketplaceData(customHigh.getId(), 1),
        createMarketplaceData(customLow.getId(), 2),
        createMarketplaceData(nullAlpha.getId(), null),
        createMarketplaceData(nullZulu.getId(), null)));

    ProductSearchCriteria criteria = new ProductSearchCriteria();
    Page<Product> page = repository.searchByCriteria(criteria,
        PageRequest.of(0, 20, Sort.by(SortOption.STANDARD.getOption()).descending()));

    assertThat(page.getContent())
        .as("standard sort should keep custom ordered products first and alphabetize the null remainder")
        .extracting(Product::getId)
        .containsSubsequence(
            customHigh.getId(),
            customLow.getId(),
            nullAlpha.getId(),
            nullZulu.getId());
  }

  private static Product createProduct(String id, String englishName) {
    Product product = new Product();
    product.setId(id);
    product.setListed(true);
    product.setType("utils");
    product.setMarketDirectory("market/utils/" + id + "/");
    product.setTags(List.of("utils"));
    product.setReleasedVersions(List.of("1.0.0"));

    Map<String, String> names = new HashMap<>();
    names.put("en", englishName);
    product.setNames(names);

    Map<String, String> shortDescriptions = new HashMap<>();
    shortDescriptions.put("en", englishName + " description");
    product.setShortDescriptions(shortDescriptions);
    return product;
  }

  private static ProductMarketplaceData createMarketplaceData(String id, Integer customOrder) {
    ProductMarketplaceData productMarketplaceData = new ProductMarketplaceData();
    productMarketplaceData.setId(id);
    productMarketplaceData.setInstallationCount(0);
    productMarketplaceData.setCustomOrder(customOrder);
    return productMarketplaceData;
  }
}
