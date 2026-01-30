package com.axonivy.market.core.exceptions.model;

import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class NotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String code;
  private final String message;

  public NotFoundException(ErrorCode errorCode) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText();
  }

  public NotFoundException(ErrorCode errorCode, String additionalMessage) {
    this.code = errorCode.getCode();
    this.message = errorCode.getHelpText() + CoreCommonConstants.DASH_SEPARATOR + additionalMessage;
  }

}
