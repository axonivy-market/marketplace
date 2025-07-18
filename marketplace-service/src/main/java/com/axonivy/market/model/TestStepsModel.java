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

  public static TestStepsModel from(TestStep entity) {
    return TestStepsModel.builder()
        .name(entity.getName())
        .status(entity.getStatus())
        .type(entity.getType())
        .testType(entity.getTestType())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestStepsModel that = (TestStepsModel) o;

    if (!name.equals(that.name)) {
      return false;
    }
    if (status != that.status) {
      return false;
    }
    if (type != that.type) {
      return false;
    }
    return testType == that.testType;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + status.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + testType.hashCode();
    return result;
  }

}
