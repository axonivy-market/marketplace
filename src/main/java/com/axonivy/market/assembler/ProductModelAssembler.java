package com.axonivy.market.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductModel;

@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductModel> {

  public ProductModelAssembler() {
    super(ProductDetailsController.class, ProductModel.class);
  }

  @Override
  public ProductModel toModel(Product product) {
    ProductModel resource = new ProductModel();
    resource.add(linkTo(methodOn(ProductDetailsController.class).findProduct(product.getId(), product.getType()))
        .withSelfRel());
    return createResource(resource, product);
  }

  private ProductModel createResource(ProductModel model, Product product) {
    model.setId(product.getId());
    model.setName(product.getName());
    model.setShortDescription(product.getShortDescription());
    model.setType(product.getType());
    model.setTags(product.getTags());
    model.setLogoUrl(product.getLogoUrl());
    return model;
  }

}
