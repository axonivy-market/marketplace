package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.USER;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(USER)
public class User {
  @Id
  private String id;
  private String username;
  private String password;
}
