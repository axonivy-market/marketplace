package com.axonivy.market.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.util.ResourceUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubProvider {

  private static final String GITHUB_TOKEN_FILE = "classpath:github.token";

  public static GitHub get() throws IOException {
    File githubtoken = ResourceUtils.getFile(GITHUB_TOKEN_FILE);
    var token = Files.readString(githubtoken.toPath());
    return new GitHubBuilder().withOAuthToken(token).build();
  }
}
