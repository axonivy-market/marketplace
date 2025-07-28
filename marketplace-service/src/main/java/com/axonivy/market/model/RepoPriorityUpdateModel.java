package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RepoPriorityUpdateModel {
  private String repoName;
  private Integer priority;
}
