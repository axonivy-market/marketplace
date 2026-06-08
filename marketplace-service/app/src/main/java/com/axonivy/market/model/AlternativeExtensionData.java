package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AlternativeExtensionData {
  String successorUrl;
  String alternativeExtension;
  String deprecatedVersionFrom;
}
