package com.axonivy.market.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workflow")
public class WorkflowRepo extends AuditableIdEntity{

  private String type;
  private int passed;
  private int failed;
  private int mockPassed;
  private int mockFailed;
  private int realPassed;
  private int realFailed;

  @ManyToOne
  @JoinColumn(name = "github_repo")
  private GithubRepo repository;

  @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestSteps> testSteps;
}
