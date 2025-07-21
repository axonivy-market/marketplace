package com.axonivy.market.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TesResults {
  String workflow;
  String environment;
  String status;
  int count;
}
