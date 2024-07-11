package com.axonivy.market.exceptions.model;

import com.axonivy.market.enums.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private static final String SEPARATOR = "-";

  private final String code;
  private final String message;

  public NotFoundException(ErrorCode errorCode) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText();
  }

  public NotFoundException(ErrorCode errorCode, String additionalMessage) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText() + SEPARATOR + additionalMessage;
  }

}
