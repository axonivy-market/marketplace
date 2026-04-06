package com.axonivy.market.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DeprecatedResponse {
  private List<ProductDeprecationProjection> productDeprecations;
  private String pullRequestUrl;
}
