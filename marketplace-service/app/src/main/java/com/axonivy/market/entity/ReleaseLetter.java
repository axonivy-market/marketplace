package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.RELEASE_LETTER;
import static com.axonivy.market.core.constants.CoreEntityConstants.TEXT_TYPE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = RELEASE_LETTER)
public class ReleaseLetter extends AuditableIdEntity {
  @Serial
  private static final long serialVersionUID = 1;
  private String sprint;

  @Column(columnDefinition = TEXT_TYPE)
  private String content;

  private boolean isActive;
}
