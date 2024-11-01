package com.axonivy.market.service.impl;

import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ProductJsonContentService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductJsonContentServiceImpl implements ProductJsonContentService {
  private final ProductJsonContentRepository productJsonRepo;

  @Override
  public void updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion,
      String productId, String productName) {
    if (ObjectUtils.isNotEmpty(jsonContent)) {
      ProductJsonContent productJsonContent = new ProductJsonContent();
      productJsonContent.setVersion(currentVersion);
      productJsonContent.setProductId(productId);
      ProductFactory.mappingIdForProductJsonContent(productJsonContent);
      productJsonContent.setName(productName);
      productJsonContent.setContent(jsonContent.replace(replaceVersion, currentVersion));
      productJsonRepo.save(productJsonContent);
    }
  }
}
