package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum FeedbackStatus {
  APPROVED("approved"), PENDING("pending"), REJECTED("rejected");

  private final String code;

  public static FeedbackStatus of(String code) {
    for (var status : values()) {
      if (StringUtils.equalsIgnoreCase(code, status.code)) {
        return status;
      }
    }
    throw new NotFoundException(ErrorCode.GH_FILE_STATUS_INVALID, "FeedbackStatus: " + code);
  }
}
