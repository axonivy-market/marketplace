package com.axonivy.market.entity;

import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.WorkFlowType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "test_step")
public class TestStep extends AuditableIdEntity implements Serializable {

  private String name;
  @Enumerated(EnumType.STRING)
  private TestStatus status;
  private WorkFlowType type;
  @Enumerated(EnumType.STRING)
  private TestEnviroment testType;
  @ManyToOne
  private GithubRepo repository;
}
