package com.axonivy.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axonivy.market.entity.ProductDesignerInstallation;
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

  protected List<ProductDesignerInstallation> createProductDesignerInstallationsMock() {
    var mockProductDesignerInstallations = new ArrayList<ProductDesignerInstallation>();
    ProductDesignerInstallation mockProductDesignerInstallation = new ProductDesignerInstallation();
    mockProductDesignerInstallation.setProductId(SAMPLE_PRODUCT_ID);
    mockProductDesignerInstallation.setDesignerVersion("10.0.22");
    mockProductDesignerInstallation.setInstallationCount(50);
    mockProductDesignerInstallations.add(mockProductDesignerInstallation);

    mockProductDesignerInstallation = new ProductDesignerInstallation();
    mockProductDesignerInstallation.setProductId(SAMPLE_PRODUCT_ID);
    mockProductDesignerInstallation.setDesignerVersion("11.4.0");
    mockProductDesignerInstallation.setInstallationCount(30);
    mockProductDesignerInstallations.add(mockProductDesignerInstallation);
    return mockProductDesignerInstallations;
  }
}
