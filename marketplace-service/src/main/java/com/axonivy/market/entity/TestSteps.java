package com.axonivy.market.entity;

import com.axonivy.market.enums.TestStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
