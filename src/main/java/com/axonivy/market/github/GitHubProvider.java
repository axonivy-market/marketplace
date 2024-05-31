package com.axonivy.market.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.util.ResourceUtils;

public class GitHubProvider {

  public static GitHub get() {
    try {
      File githubtoken = null;

      githubtoken = ResourceUtils.getFile("classpath:github.token");

      var token = Files.readString(githubtoken.toPath());
      return new GitHubBuilder().withOAuthToken(token).build();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
