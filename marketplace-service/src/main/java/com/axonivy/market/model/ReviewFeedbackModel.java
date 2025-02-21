package com.axonivy.market.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFeedbackModel {
  private String feedbackId;

  private Boolean isApproved;

  private String moderatorName;
}
