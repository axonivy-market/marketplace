package com.axonivy.market.stable.assembler;

import com.axonivy.market.core.config.MarketplaceConfig;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductDetailModel;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductDetailModelAssembler implements RepresentationModelAssembler<Product, ProductDetailModel> {
  private final MarketplaceConfig marketplaceConfig;

  @Override
  public ProductDetailModel toModel(Product product) {
    return ProductDetailModel.createModel(product, marketplaceConfig.isProduction());
  }
}
