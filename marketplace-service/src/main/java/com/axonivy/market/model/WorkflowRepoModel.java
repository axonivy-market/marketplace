package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRepoModel extends RepresentationModel<WorkflowRepoModel> {

  @Schema(description = "Workflow type", example = "CI or DEV")
  private String type;

  @Schema(description = "Number of passed tests", example = "10")
  private int passed;

  @Schema(description = "Number of failed tests", example = "2")
  private int failed;

  @Schema(description = "Number of passed mock tests", example = "3")
  private int mockPassed;

  @Schema(description = "Number of failed mock tests", example = "1")
  private int mockFailed;

  @Schema(description = "Number of passed real tests", example = "7")
  private int realPassed;

  @Schema(description = "Number of failed real tests", example = "1")
  private int realFailed;
}