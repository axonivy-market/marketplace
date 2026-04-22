package com.axonivy.market.entity;

import com.axonivy.market.core.entity.AbstractAuditableEntity;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.SecretScanning;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_SECURITY_INFO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = PRODUCT_SECURITY_INFO)
public class ProductSecurityInfo extends AbstractAuditableEntity<String> {
  @Serial
  private static final long serialVersionUID = 1;

  @Id
  private String repoName;
  private boolean isArchived;
  private String visibility;
  private boolean branchProtectionEnabled;
  private Date lastCommitDate;
  private String latestCommitSHA;

  @Embedded
  @AttributeOverride(name = "alerts", column = @Column(name = "dependabot_alerts"))
  @AttributeOverride(name = "status", column = @Column(name = "dependabot_status"))
  private Dependabot dependabot;

  @Embedded
  @AttributeOverride(name = "alerts", column = @Column(name = "code_scanning_alerts"))
  @AttributeOverride(name = "status", column = @Column(name = "code_scanning_status"))
  private CodeScanning codeScanning;

  @Embedded
  private SecretScanning secretScanning;

  @Override
  public String getId() {
    return repoName;
  }

  @Override
  public void setId(String id) {
    this.repoName = id;
  }
}
