package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class DuplicatedEntityException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 6778659816851728814L;

  private final String message;

}