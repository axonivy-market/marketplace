package com.axonivy.market.entity;

import com.axonivy.market.enums.TestStatus;
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
  @Schema(description = "CI workflow badge URL", example = "https://github.com/actions/workflows/ci.yml/badge.svg")
  private String badgeUrl;
  private Map<String, Integer> results;
}
