package com.axonivy.market.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "workflow")
public class WorkflowRepository extends AuditableIdEntity{

  @Id
  private String id;
  private String type;
  private int passed;
  private int failed;
  private int mockPassed;
  private int mockFailed;
  private int realPassed;
  private int realFailed;

  @ManyToOne
  @JoinColumn(name = "repository_name")
  private Repository repository;

  @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestSteps> testSteps;
}
