package com.axonivy.market.entity;

import com.axonivy.market.core.entity.EntityConstants;
import com.axonivy.market.core.entity.GenericIdEntity;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = EntityConstants.TEST_STEP)
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
