package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum FileStatus {
  MODIFIED("modified"), ADDED("added"), REMOVED("removed");

  private final String code;

  public static FileStatus of(String code) {
    for (var status : values()) {
      if (StringUtils.equalsIgnoreCase(code, status.code)) {
        return status;
      }
    }
    throw new NotFoundException(ErrorCode.GH_FILE_STATUS_INVALID, "FileStatus: " + code);
  }
}
