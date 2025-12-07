package com.axonivy.market.exceptions.model;

import com.axonivy.market.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class FileProcessingException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  private final String code;
  private final String message;

  public FileProcessingException(ErrorCode errorCode) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText();
  }
}