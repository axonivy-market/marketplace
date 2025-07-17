package com.axonivy.market.model;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.WorkFlowType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStepsModel extends RepresentationModel<TestStepsModel> {
  @Schema(description = "Test Name", example = "chatWithAssistant")
  private String name;

  @Schema(description = "Test Status", example = "PASSED or FAILED or SKIPPED")
  private TestStatus status;

  @Schema(description = "Workflow type", example = "CI or DEV")
  private WorkFlowType type;

  @Schema(description = "Type of test", example = "MOCK or REAL")
  private TestEnviroment testType;

  public static TestStepsModel createTestStepsModel(TestStep entity) {
    return TestStepsModel.builder()
        .name(entity.getName())
        .status(entity.getStatus())
        .type(entity.getType())
        .testType(entity.getTestType())
        .build();
  }
}
