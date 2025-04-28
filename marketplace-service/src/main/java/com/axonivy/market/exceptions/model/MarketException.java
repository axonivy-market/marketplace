package com.axonivy.market.exceptions.model;

import com.axonivy.market.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MarketException extends RuntimeException {

  private final String code;
  private final String message;

  public MarketException(ErrorCode errorCode) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText();
  }
}
