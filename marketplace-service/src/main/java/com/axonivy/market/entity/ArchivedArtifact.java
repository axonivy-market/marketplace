package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.ARCHIVED_ARTIFACT;
import static com.axonivy.market.constants.EntityConstants.ARTIFACT_ID_FK;

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

  @ManyToOne
  @JoinColumn(name = ARTIFACT_ID_FK, nullable = false)
  @JsonBackReference
  private Artifact artifact;

}