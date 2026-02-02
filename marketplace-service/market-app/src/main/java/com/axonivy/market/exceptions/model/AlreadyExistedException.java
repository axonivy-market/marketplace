package com.axonivy.market.exceptions.model;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
public class AlreadyExistedException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String code;
  private final String message;
}
