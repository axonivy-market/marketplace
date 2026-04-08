package com.axonivy.market.model;

import com.axonivy.market.enums.PullRequestAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeprecationRequest {
  private String successorUrl;
  private Boolean isDeprecated;
  private boolean addReadme;
  private PullRequestAction pullRequestAction;
  private String deprecationRequester;
}
