package com.axonivy.market.assembler;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<Product, ProductModel> {

  @Override
  public ProductModel toModel(Product product) {
    var resource = new ProductModel();
    resource.add(linkTo(methodOn(ProductDetailsController.class)
            .findProductDetails(product.getId(),false)).withSelfRel());
    return ProductModel.createResource(resource, product);
  }

}
