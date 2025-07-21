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
  WorkFlowType workflow;
  TestEnviroment environment;
  TestStatus status;
  int count;
}
