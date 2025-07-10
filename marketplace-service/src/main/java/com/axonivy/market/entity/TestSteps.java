package com.axonivy.market.entity;

import com.axonivy.market.enums.TestStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_step")
public class TestSteps extends AuditableIdEntity{

  @Id
  private String id;
  private String name;

  @Enumerated(EnumType.STRING)
  private TestStatus status;

  @ManyToOne
  @JoinColumn(name = "workflow_id")
  private WorkflowRepository workflow;
}
