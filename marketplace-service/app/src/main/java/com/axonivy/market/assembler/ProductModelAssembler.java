package com.axonivy.market.assembler;

import com.axonivy.market.controller.ImageController;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.model.ProductModel;
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

    resource.setId(product.getId());
    resource.setNames(product.getNames());
    resource.setShortDescriptions(product.getShortDescriptions());
    resource.setType(product.getType());
    resource.setTags(product.getTags());
    resource.setMarketDirectory(product.getMarketDirectory());

    var logoLink = linkTo(methodOn(ImageController.class).findImageById(product.getLogoId())).withSelfRel();
    resource.setLogoUrl(logoLink.getHref());

    return resource;
  }

}
