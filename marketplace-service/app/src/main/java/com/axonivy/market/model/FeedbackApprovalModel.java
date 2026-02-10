package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackApprovalModel {
  private String productId;

  private String userId;

  private String feedbackId;

  private Boolean isApproved;

  private String moderatorName;

  private Integer version;
}
