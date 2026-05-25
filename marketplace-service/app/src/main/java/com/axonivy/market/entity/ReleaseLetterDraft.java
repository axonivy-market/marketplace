package com.axonivy.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

import static com.axonivy.market.constants.EntityConstants.RELEASE_LETTER_DRAFTS;
import static com.axonivy.market.core.constants.CoreEntityConstants.TEXT_TYPE;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = RELEASE_LETTER_DRAFTS)
public class ReleaseLetterDraft extends AuditableIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String gitHubUserId;
  private String releaseLetterId;

  @Column(columnDefinition = TEXT_TYPE)
  private String draftContent;
}
