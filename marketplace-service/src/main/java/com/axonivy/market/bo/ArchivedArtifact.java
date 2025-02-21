package com.axonivy.market.bo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "archived_artifact")
public class ArchivedArtifact implements Serializable {
  @Id
  private String id;

  @Serial
  private static final long serialVersionUID = 1L;
  private String lastVersion;
  private String groupId;
  private String artifactId;

  @ManyToOne
  @JoinColumn(name = "artifact_id_fk", nullable = false)
  @JsonBackReference
  private Artifact artifact;

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}