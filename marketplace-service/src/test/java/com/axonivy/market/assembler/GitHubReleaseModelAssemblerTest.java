package com.axonivy.market.assembler;

import com.axonivy.market.model.GitHubReleaseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
class GitHubReleaseModelAssemblerTest {
  @InjectMocks
  private GithubReleaseModelAssembler githubReleaseModelAssembler;

  @BeforeEach
  void setup() {
    githubReleaseModelAssembler = new GithubReleaseModelAssembler();
  }

  @Test
  void testToModel() {
    GitHubReleaseModel model = new GitHubReleaseModel();
    model.setBody("Github body");
    model.setName("v1.0.0");
    model.setPublishedAt(new Date());
    GitHubReleaseModel result = githubReleaseModelAssembler.toModel(model);
    Assertions.assertEquals(model.getBody(), result.getBody());
    Assertions.assertEquals(model.getName(), result.getName());
    Assertions.assertEquals(model.getPublishedAt(), result.getPublishedAt());
    Assertions.assertEquals(model, result, "The toModel method should return the same GithubReleaseModel object " +
        "passed to it");
  }
}
