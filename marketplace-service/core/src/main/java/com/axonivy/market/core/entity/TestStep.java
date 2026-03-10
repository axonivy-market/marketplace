package com.axonivy.market.core.entity;

import static com.axonivy.market.core.constants.CoreEntityConstants.TEST_STEP;

import com.axonivy.market.core.enums.TestStatus;
import com.axonivy.market.core.enums.WorkFlowType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = TEST_STEP)
public class TestStep extends GenericIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;
  private String name;
  @Enumerated(EnumType.STRING)
  private TestStatus status;
  @Enumerated(EnumType.STRING)
  private WorkFlowType type;

  public static TestStep createTestStep(String name, TestStatus status, WorkFlowType workflowType) {
    return TestStep.builder()
        .name(name)
        .status(status)
        .type(workflowType)
        .build();
  }
}
