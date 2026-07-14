package com.axonivy.market.entity;

import com.axonivy.market.core.constants.CoreEntityConstants;
import com.axonivy.market.core.entity.EntityConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = EntityConstants.RELEASE_LETTER_DRAFTS)
public class ReleaseLetterDraft extends AuditableIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String gitHubUserId;
  private String releaseLetterId;

  @Column(columnDefinition = CoreEntityConstants.TEXT_TYPE)
  private String draftContent;
}
