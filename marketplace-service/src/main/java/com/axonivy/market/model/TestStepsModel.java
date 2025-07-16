package com.axonivy.market.model;

import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.enums.TestStatus;
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

  public static TestStepsModel createModel(TestSteps entity) {
    return TestStepsModel.builder()
        .name(entity.getName())
        .status(entity.getStatus())
        .build();
  }
}
