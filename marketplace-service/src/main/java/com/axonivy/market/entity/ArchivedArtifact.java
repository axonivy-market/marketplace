package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

import static com.axonivy.market.constants.EntityConstants.ARCHIVED_ARTIFACT;
import static com.axonivy.market.constants.EntityConstants.ARTIFACT_ID_FK;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = ARCHIVED_ARTIFACT)
public class ArchivedArtifact implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Serial
  private static final long serialVersionUID = 1L;
  private String lastVersion;
  private String groupId;
  private String artifactId;

  @ManyToOne
  @JoinColumn(name = ARTIFACT_ID_FK, nullable = false)
  @JsonBackReference
  private Artifact artifact;
  
}