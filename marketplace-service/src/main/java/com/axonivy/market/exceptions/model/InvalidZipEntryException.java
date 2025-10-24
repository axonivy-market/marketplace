package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvalidZipEntryException extends RuntimeException {
  public InvalidZipEntryException(String message) {
    super(message);
  }
}
