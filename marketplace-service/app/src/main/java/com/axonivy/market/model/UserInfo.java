package com.axonivy.market.model;

import static com.axonivy.market.constants.EntityConstants.GITHUB_USER;

import com.axonivy.market.entity.GithubUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = GITHUB_USER)
public class UserInfo extends GithubUser {
  @Serial
  private static final long serialVersionUID = 1;

  private String token;
}
