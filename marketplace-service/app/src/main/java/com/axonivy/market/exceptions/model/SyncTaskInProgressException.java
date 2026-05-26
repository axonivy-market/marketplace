package com.axonivy.market.exceptions.model;

public class SyncTaskInProgressException extends RuntimeException {
  public SyncTaskInProgressException(String message) {
    super(message);
  }
}
