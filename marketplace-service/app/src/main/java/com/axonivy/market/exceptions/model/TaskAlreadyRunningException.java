package com.axonivy.market.exceptions.model;

public class TaskAlreadyRunningException extends RuntimeException {
  public TaskAlreadyRunningException(String message) {
    super(message);
  }
}
