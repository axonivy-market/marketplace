package com.axonivy.market.enums;

import lombok.Getter;

/**
 * <p>
 * Workflow status enumeration defining the operational status of GitHub workflows.
 * </p>
 *
 * @since 15/04/2026
 * @author nntthuy
 */
@Getter
public enum WorkflowStatus {
  ACTIVE("active"),
  DISABLED_INACTIVITY("disabled_inactivity"),
  DISABLED_MANUALLY("disabled_manually"),
  DISABLED_UNKNOWN("disabled_unknown"),
  DELETED("deleted");

  private final String status;

  WorkflowStatus(String status) {
    this.status = status;
  }
}