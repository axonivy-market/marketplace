package com.axonivy.market.assembler;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.model.TestStepsModel;
import lombok.NonNull;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TestStepsModelAssembler implements RepresentationModelAssembler<TestStep, TestStepsModel> {

  @Override
  @NonNull
  public TestStepsModel toModel(@NonNull TestStep step) {
    return TestStepsModel.from(step);
  }

}
