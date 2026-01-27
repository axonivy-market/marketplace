package com.axonivy.market.assembler;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;

import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.axonivy.market.config.MarketplaceConfig;

@RequiredArgsConstructor
@Component
public class ProductDetailModelAssembler implements RepresentationModelAssembler<Product, ProductDetailModel> {
  private final MarketplaceConfig marketplaceConfig;

  @Override
  public ProductDetailModel toModel(Product product) {
    return ProductDetailModel.createModel(product, marketplaceConfig.isProduction());
  }
}
