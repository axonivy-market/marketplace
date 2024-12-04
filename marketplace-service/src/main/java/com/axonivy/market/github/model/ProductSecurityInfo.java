package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ProductSecurityInfo {
  private String repoName;
  private boolean isArchived;
  private String visibility;
  private boolean branchProtectionEnabled;
  private Date lastCommitDate;
  private String latestCommitSHA;
  private Map<String, Integer> vulnerabilities;
  private Dependabot dependabot;
  private SecretScanning secretsScanning;
  private CodeScanning codeScanning;
}
