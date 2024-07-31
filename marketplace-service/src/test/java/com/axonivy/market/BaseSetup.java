package com.axonivy.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;

public class BaseSetup {
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
  protected static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());

  protected Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Map<String, String> name = new HashMap<>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.put(Language.EN.getValue(), SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new HashMap<>();
    name.put(Language.EN.getValue(), "Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }
}
