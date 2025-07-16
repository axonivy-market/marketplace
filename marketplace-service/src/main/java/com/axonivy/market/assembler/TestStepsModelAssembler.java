package com.axonivy.market.assembler;

import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.model.TestStepsModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TestStepsModelAssembler implements RepresentationModelAssembler<TestSteps, TestStepsModel> {
  @Override
  public TestStepsModel toModel(TestSteps entity) {
    return TestStepsModel.createModel(entity);
  }
}
