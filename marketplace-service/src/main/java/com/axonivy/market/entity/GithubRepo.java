package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "github_repo")
public class GithubRepo extends AuditableIdEntity{

  private String name;
  private String htmlUrl;
  private String language;
  private Date lastUpdated;
  private String ciBadgeUrl;
  private String devBadgeUrl;
  @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestSteps> testSteps;
}
