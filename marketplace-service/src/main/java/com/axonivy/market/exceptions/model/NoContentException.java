package com.axonivy.market.exceptions.model;

import com.axonivy.market.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class NoContentException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String SEPARATOR = "-";

  private final String code;
  private final String message;

  public NoContentException(ErrorCode errorCode, String additionalMessage) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText() + SEPARATOR + additionalMessage;
  }

}
