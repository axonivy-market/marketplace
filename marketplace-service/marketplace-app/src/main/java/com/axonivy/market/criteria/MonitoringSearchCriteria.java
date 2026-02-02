package com.axonivy.market.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringSearchCriteria {
  private Boolean isFocused;
  private String searchText;
  private String workFlowType;
  private String sortDirection;
}
