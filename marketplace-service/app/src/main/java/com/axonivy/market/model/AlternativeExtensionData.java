package com.axonivy.market.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AlternativeExtensionData {
  private String successorUrl;
  private String alternativeExtension;
  private String deprecatedVersionFrom;
}
