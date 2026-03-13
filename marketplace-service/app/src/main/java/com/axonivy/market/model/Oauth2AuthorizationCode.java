package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Oauth2AuthorizationCode {
  @Schema(description = "Exchange code")
  private String code;
}
