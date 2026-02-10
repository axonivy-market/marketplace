package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.enums.TypeOption;
import com.axonivy.market.core.repository.CoreProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreProductServiceImplTest extends CoreBaseSetup {
  @Mock
  private CoreProductRepository productRepo;
  @Spy
  @InjectMocks
  private CoreProductServiceImpl productService;
  private String language;
  private String keyword;
  private Page<Product> mockResultReturn;

  @BeforeEach
  void setup() {
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testFindProducts() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "All products should match mock result");
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "Connector type products should match mock result");
    result = productService.findProducts(TypeOption.DEMOS.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(2, result.getSize(), "Number of demo type products should match number of mock demo type result");
  }

  @Test
  void testFindAllProductsWithKeyword() {
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);
    var result = productService.findProducts(TypeOption.ALL.getOption(), keyword, language, false, PAGEABLE);
    assertEquals(mockResultReturn, result, "All products should match mock result");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));

    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(new PageImpl<>(
        mockResultReturn.stream().filter(
            product -> product.getNames().get(Language.EN.getValue()).equals(SAMPLE_PRODUCT_NAME)).toList()));
    result = productService.findProducts(TypeOption.ALL.getOption(), SAMPLE_PRODUCT_NAME, language, false, PAGEABLE);
    assertTrue(result.hasContent(), "Result product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(new PageImpl<>(
        mockResultReturn.stream().filter(product -> product.getNames().get(Language.EN.getValue()).equals(
            SAMPLE_PRODUCT_NAME) && product.getType().equals(TypeOption.CONNECTORS.getCode())).toList()));
    result = productService.findProducts(TypeOption.CONNECTORS.getOption(), SAMPLE_PRODUCT_NAME, language, false,
        PAGEABLE);
    assertTrue(result.hasContent(), "Result connector type product list should not be empty");
    assertEquals(SAMPLE_PRODUCT_NAME, result.getContent().get(0).getNames().get(Language.EN.getValue()),
        "First product should match mock product name");
  }

  @Test
  void testSearchProducts() {
    var simplePageable = PageRequest.of(0, 20);
    String type = TypeOption.ALL.getOption();
    keyword = "on";
    language = "en";
    when(productRepo.searchByCriteria(any(), any(Pageable.class))).thenReturn(mockResultReturn);

    var result = productService.findProducts(type, keyword, language, false, simplePageable);
    assertEquals(result, mockResultReturn, "Product list from search query should match mock product list");
    verify(productRepo).searchByCriteria(any(), any(Pageable.class));
  }

  protected Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Map<String, String> name = new HashMap<>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.put(Language.EN.getValue(), SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new HashMap<>();
    name.put(Language.EN.getValue(), "Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }
}
