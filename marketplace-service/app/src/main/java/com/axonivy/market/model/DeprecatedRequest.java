package com.axonivy.market.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeprecatedRequest {
  private String productId;
  private String successorUrl;
  private Boolean deprecated;
  private boolean addReadme;
}
