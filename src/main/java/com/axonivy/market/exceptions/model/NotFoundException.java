package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String message;

}
