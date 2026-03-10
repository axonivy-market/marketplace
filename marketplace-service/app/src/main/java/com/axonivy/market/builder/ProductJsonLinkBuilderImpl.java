package com.axonivy.market.builder;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.core.builder.ProductJsonLinkBuilder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.stereotype.Component;

@Component
public class ProductJsonLinkBuilderImpl implements ProductJsonLinkBuilder {
  @Override
  public String buildProductJsonUrl(
      String productId,
      String version,
      String designerVersion) {

    var link = linkTo(
        methodOn(ProductDetailsController.class)
            .findProductJsonContent(productId, version, designerVersion))
        .withSelfRel();

    return link.getHref();
  }
}
