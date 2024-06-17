package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class NotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String message;

}
