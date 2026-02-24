package com.axonivy.market.github.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSecurityInfo {
  private String repoName;
  private boolean isArchived;
  private String visibility;
  private boolean branchProtectionEnabled;
  private Date lastCommitDate;
  private String latestCommitSHA;
  private Dependabot dependabot;
  private SecretScanning secretScanning;
  private CodeScanning codeScanning;
}
