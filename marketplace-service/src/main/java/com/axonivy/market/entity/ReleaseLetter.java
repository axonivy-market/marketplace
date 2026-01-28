package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

import static com.axonivy.market.constants.EntityConstants.RELEASE_LETTER;

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
  private String releaseVersion;
  private String content;
}
