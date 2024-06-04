package com.axonivy.market.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class GithubRepoMeta {
  private String repoName;
  private Long lastChange;
}
