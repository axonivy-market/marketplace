package com.axonivy.market.builder;

import com.axonivy.market.controller.ImageController;
import com.axonivy.market.core.builder.ImageLinkBuilder;
import com.axonivy.market.core.utils.CoreImageUtils;
import jakarta.annotation.PostConstruct;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.stereotype.Service;

@Service
public class ImageLinkBuilderImpl implements ImageLinkBuilder {
  @PostConstruct
  public void register() {
    CoreImageUtils.setImageLinkBuilder(this);
  }

  @Override
  public String createImageUrl(String imageId) {
    return linkTo(methodOn(ImageController.class)
        .findImageById(imageId))
        .withSelfRel()
        .getHref();
  }

  @Override
  public String createImageUrlForProduction(String imageId) {
    return linkTo(methodOn(ImageController.class)
        .findImageById(imageId))
        .toUriComponentsBuilder()
        .build()
        .getPath();
  }
}
