package com.axonivy.market.assembler;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailModelAssembler implements RepresentationModelAssembler<Product, ProductDetailModel> {

  @Override
  public ProductDetailModel toModel(Product product) {
    return ProductDetailModel.createModel(product);
  }

}
