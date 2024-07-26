package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class UnauthorizedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 6778659812221728814L;

  private final String error;
  private final String message;
}
