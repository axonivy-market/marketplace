package com.axonivy.market.service.impl;

import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductJsonContentService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import static com.axonivy.market.constants.ProductJsonConstants.EN_LANGUAGE;

@Service
@AllArgsConstructor
public class ProductJsonContentServiceImpl implements ProductJsonContentService {
  private final ProductJsonContentRepository productJsonRepo;
  private final ProductRepository productRepo;

  @Override
  public void updateProductJsonContent(String jsonContent, String currentVersion,
      String replaceVersion, String productId) {
    if (ObjectUtils.isNotEmpty(jsonContent)) {
      productRepo.findById(productId).ifPresent(product -> {
            ProductJsonContent productJsonContent = new ProductJsonContent();
            productJsonContent.setVersion(currentVersion);
            productJsonContent.setProductId(productId);
            ProductFactory.mappingIdForProductJsonContent(productJsonContent);
            productJsonContent.setName(product.getNames().get(EN_LANGUAGE));
            productJsonContent.setContent(jsonContent.replace(replaceVersion, currentVersion));
            productJsonRepo.save(productJsonContent);
          }
      );
    }
  }
}
