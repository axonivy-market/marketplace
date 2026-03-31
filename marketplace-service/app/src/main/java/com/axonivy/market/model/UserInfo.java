package com.axonivy.market.model;

import com.axonivy.market.entity.GithubUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo extends GithubUser {
  @Serial
  private static final long serialVersionUID = 1;

  private String token;
  private String url;
}
