package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Feedback status enumeration defining the approval workflow states of user feedback submissions.
 * </p>
 *
 * @since 15/04/2026
 * @author nntthuy
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatus {
  APPROVED, PENDING, REJECTED
}
