package com.axonivy.market.model;

import com.axonivy.market.enums.PullRequestAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeprecatedRequest {
  private String productId;
  private String successorUrl;
  private Boolean deprecated;
  private boolean addReadme;
  private PullRequestAction pullRequestAction;
  private String deprecationRequester;
}
