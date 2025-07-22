package com.axonivy.market.entity;

import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class TesResults {
  private WorkFlowType workflow;
  private String environment;
  private TestStatus status;
  private int count;

}
