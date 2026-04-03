package com.axonivy.market.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DeprecatedResponse {
  List<ProductDeprecationProjection> productDeprecations;
  String pullRequestUrl;
}
