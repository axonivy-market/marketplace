package com.axonivy.market.model;

import com.axonivy.market.enums.WorkFlowType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestResults {
  private WorkFlowType workflow;
  private Map<String, Integer> results;
}
