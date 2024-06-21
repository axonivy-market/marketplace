package com.axonivy.market.exceptions.model;

import com.axonivy.market.enums.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidParamException extends NotFoundException {
  private static final long serialVersionUID = 1L;

  public InvalidParamException(String code, String message) {
    super(code, message);
  }

  public InvalidParamException(ErrorCode errorCode) {
    super(errorCode);
  }

  public InvalidParamException(ErrorCode errorCode, String additionalMessage) {
    super(errorCode, additionalMessage);
  }
}
