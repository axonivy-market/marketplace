package com.axonivy.market.core.builder;

public interface ProductJsonLinkBuilder {
  String buildProductJsonUrl(String productId, String version, String designerVersion);
}
