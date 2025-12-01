package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class IOException extends RuntimeException {

  private final String code;
  private final String message;
}
