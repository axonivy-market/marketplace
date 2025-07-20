package com.axonivy.market.entity;

import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

import static com.axonivy.market.constants.EntityConstants.TEST_STEP;

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
  @Enumerated(EnumType.STRING)
  private TestEnviroment testType;
}
