package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.ARCHIVED_ARTIFACT;

import java.io.Serial;

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
