package com.axonivy.market.assembler;

import com.axonivy.market.controller.ImageController;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductModel> {

  public ProductModelAssembler() {
    super(ProductDetailsController.class, ProductModel.class);
  }

  @Override
  public ProductModel toModel(Product product) {
    ProductModel resource = new ProductModel();
    resource.add(linkTo(methodOn(ProductDetailsController.class).findProductDetails(product.getId(),false)).withSelfRel());
    return createResource(resource, product);
  }

  public ProductModel createResource(ProductModel model, Product product) {
    model.setId(product.getId());
    model.setNames(product.getNames());
    model.setShortDescriptions(product.getShortDescriptions());
    model.setType(product.getType());
    model.setTags(product.getTags());

    Link logoLink = linkTo(methodOn(ImageController.class).findImageById(product.getLogoId())).withSelfRel();
    model.setLogoUrl(logoLink.getHref());
    return model;
  }

}
