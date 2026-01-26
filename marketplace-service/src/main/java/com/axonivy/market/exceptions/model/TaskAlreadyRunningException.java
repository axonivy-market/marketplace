package com.axonivy.market.exceptions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaskAlreadyRunningException extends RuntimeException {
  public TaskAlreadyRunningException(String message) {
    super(message);
  }
}
