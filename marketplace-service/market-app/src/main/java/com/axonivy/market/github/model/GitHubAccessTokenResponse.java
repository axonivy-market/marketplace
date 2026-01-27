package com.axonivy.market.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubAccessTokenResponse {
  @JsonProperty("error")
  private String error;

  @JsonProperty("error_description")
  private String errorDescription;

  @JsonProperty("access_token")
  private String accessToken;
}
