package com.axonivy.market.model;

import com.axonivy.market.entity.GithubUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo extends GithubUser implements AuthenticatedPrincipal {
  @Serial
  private static final long serialVersionUID = 1;

  private String token;
  private String url;
  private boolean hasPasskey;

  @Override
  public String getName() {
    return super.getUsername();
  }
}
