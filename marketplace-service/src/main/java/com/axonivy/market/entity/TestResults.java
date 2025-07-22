package com.axonivy.market.entity;

import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class TestResults {
  private WorkFlowType workflow;
  private String environment;
  private TestStatus status;
  private int count;

}
