package com.axonivy.market.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

import static com.axonivy.market.core.constants.CoreEntityConstants.ARCHIVED_ARTIFACT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = ARCHIVED_ARTIFACT)
public class ArchivedArtifact extends GenericIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String lastVersion;
  private String groupId;
  private String artifactId;
}
