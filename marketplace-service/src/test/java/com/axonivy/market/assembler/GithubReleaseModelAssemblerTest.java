package com.axonivy.market.assembler;

import com.axonivy.market.entity.Product;
import com.axonivy.market.model.GithubReleaseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GithubReleaseModelAssemblerTest {
  @InjectMocks
  private GithubReleaseModelAssembler githubReleaseModelAssembler;

  @BeforeEach
  void setup() {
    githubReleaseModelAssembler = new GithubReleaseModelAssembler();
  }

  @Test
  void testToModel() {
    GithubReleaseModel model = new GithubReleaseModel();
    model.setBody("Github body");
    model.setName("v1.0.0");
    model.setPublishedAt(new Date());
    GithubReleaseModel result = githubReleaseModelAssembler.toModel(model);
    Assertions.assertEquals(model.getBody(), result.getBody());
    Assertions.assertEquals(model.getName(), result.getName());
    Assertions.assertEquals(model.getPublishedAt(), result.getPublishedAt());
    Assertions.assertEquals(model, result, "The toModel method should return the same GithubReleaseModel object " +
        "passed to it");
  }
}
