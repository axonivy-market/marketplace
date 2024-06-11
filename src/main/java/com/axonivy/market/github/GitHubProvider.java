package com.axonivy.market.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.util.ResourceUtils;

public class GitHubProvider {

  private static final String GITHUB_TOKEN_FILE = "classpath:github.token";

  public static GitHub get() {
    try {
      File githubtoken = ResourceUtils.getFile(GITHUB_TOKEN_FILE);
      var token = Files.readString(githubtoken.toPath());
      return new GitHubBuilder().withOAuthToken(token).build();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
