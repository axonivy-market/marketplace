package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * @fo {@link ErrorCode} is a presentation for a system code during proceeding
 *     data It has format cseo - 0000 c present for controller s present for
 *     service e present for entity o present for other And 0000 is a successful
 *     code
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
  SUCCESSFUL("0000", "SUCCESSFUL"), PRODUCT_FILTER_INVALID("1101", "PRODUCT_FILTER_INVALID"),
  PRODUCT_SORT_INVALID("1102", "PRODUCT_SORT_INVALID"),
  GH_FILE_STATUS_INVALID("0201", "GIT_HUB_FILE_STATUS_INVALID"),
  GH_FILE_TYPE_INVALID("0202", "GIT_HUB_FILE_TYPE_INVALID");

  String code;
  String helpText;
}
