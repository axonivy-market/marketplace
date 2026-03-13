package com.axonivy.market.core.builder;

public interface ImageLinkBuilder {

  String createImageUrl(String imageId);

  String createImageUrlForProduction(String imageId);
}
