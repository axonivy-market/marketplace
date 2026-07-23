package com.axonivy.market.enums;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;

/**
 * <p>
 * File status enumeration defining the modification states of files in GitHub repository operations.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@AllArgsConstructor
public enum FileStatus {
  MODIFIED("modified"), ADDED("added"), REMOVED("removed"), RENAMED("renamed");

  private final String code;

  public static FileStatus of(String code) {
    for (var status : values()) {
      if (Strings.CS.equals(code, status.code)) {
        return status;
      }
    }
    throw new NotFoundException(ErrorCode.GH_FILE_STATUS_INVALID, "FileStatus: " + code);
  }
}
