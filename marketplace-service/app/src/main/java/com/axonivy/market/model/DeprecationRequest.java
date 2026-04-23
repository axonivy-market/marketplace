package com.axonivy.market.model;

import com.axonivy.market.enums.PullRequestAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeprecationRequest {
  private String successorUrl;
  private Boolean isDeprecated;
  private Boolean isAddReadme;
  private PullRequestAction pullRequestAction;
  private String deprecationRequester;
  private Date deprecationDate;
}
