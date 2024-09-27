package com.axonivy.market.service.impl;

import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ProductJsonContentService;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashSet;

import static com.axonivy.market.constants.ProductJsonConstants.EN_LANGUAGE;

public class ProductJsonContentServiceImpl implements ProductJsonContentService {
  private final ProductJsonContentRepository productJsonRepo;

  public ProductJsonContentServiceImpl(
      ProductJsonContentRepository productJsonRepo) {this.productJsonRepo = productJsonRepo;}

  @Override
  public void updateProductJsonContent(ProductModuleContent productModuleContent,
      String jsonContent, String currentVersion,
      Product product) {
    if (ObjectUtils.isNotEmpty(jsonContent)) {
      ProductJsonContent productJsonContent = new ProductJsonContent();
      productJsonContent.setVersion(currentVersion);
      productJsonContent.setProductId(product.getId());
      ProductFactory.mappingIdForProductJsonContent(productJsonContent);
      productJsonContent.setName(product.getNames().get(EN_LANGUAGE));
      productJsonContent.setRelatedMavenVersions(new HashSet<>());
      productJsonContent.setContent(jsonContent.replace(ProductJsonConstants.VERSION_VALUE, currentVersion));
      productJsonRepo.save(productJsonContent);
    }
  }
}
