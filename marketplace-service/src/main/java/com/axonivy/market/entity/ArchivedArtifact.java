package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.ARCHIVED_ARTIFACT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = ARCHIVED_ARTIFACT)
public class ArchivedArtifact extends GenericIdEntity {
  private String lastVersion;
  private String groupId;
  private String artifactId;
}