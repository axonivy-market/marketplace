package com.axonivy.market.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.repository.impl.ProductSearchRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class ProductSearchRepositoryImplTest extends BaseSetup {

  Page<Product> mockResultReturn;
  ProductSearchCriteria searchCriteria;

  @Mock
  MongoTemplate mongoTemplate;

  @InjectMocks
  ProductSearchRepositoryImpl productListedRepository;

  @BeforeEach
  public void setup() {
    searchCriteria = new ProductSearchCriteria();
    mockResultReturn = createPageProductsMock();
  }

  @Test
  void testSearchByCriteria() {
    when(mongoTemplate.find(any(), eq(Product.class))).thenReturn(mockResultReturn.getContent());
    var result = productListedRepository.searchByCriteria(searchCriteria, PAGEABLE);
    assertFalse(result.isEmpty(), "Result is empty");
    assertTrue(result.isFirst(), "Result is not in first page");
    assertEquals(2, result.getContent().size());
    assertTrue(result.getContent().get(0).getNames().containsValue(SAMPLE_PRODUCT_NAME),
        "No Product has name " + SAMPLE_PRODUCT_NAME);
  }

  @Test
  void testFindByCriteria() {
    Product mockProduct = mockResultReturn.getContent().get(0);
    when(mongoTemplate.find(any(), eq(Product.class))).thenReturn(List.of(mockProduct));
    var result = productListedRepository.findByCriteria(searchCriteria);
    assertNotNull(result, "Result is empty");
    assertEquals(mockProduct.getId(), result.getId(), "Product ID " + result.getId());

    var productName = mockProduct.getNames().get(Language.EN.getValue());
    searchCriteria.setKeyword(productName);
    result = productListedRepository.findByCriteria(searchCriteria);
    assertNotNull(result, "Result is empty");
    assertEquals(productName, result.getNames().get(Language.EN.getValue()), "Product Name " + result.getNames());

    searchCriteria.setFields(List.of(DocumentField.MARKET_DIRECTORY));
    searchCriteria.setKeyword(mockProduct.getMarketDirectory());
    result = productListedRepository.findByCriteria(searchCriteria);
    assertNotNull(result, "Result is empty");
    assertEquals(mockProduct.getMarketDirectory(), result.getMarketDirectory(),
        "Product MarketDirectory " + result.getMarketDirectory());
  }
}
