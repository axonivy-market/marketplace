package com.axonivy.market.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "artifact")
public class Artifact implements Serializable {
  @Id
  private String id;
  @Serial
  private static final long serialVersionUID = 1L;
  private String repoUrl;
  private String name;
  private String groupId;
  private String artifactId;
  private String type;
  private Boolean isDependency;
  @Transient
  private Boolean isProductArtifact;

//  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//  @JoinColumn(name = "artifact_id") // Foreign key column in `archived_artifact`
//  private List<ArchivedArtifact> archivedArtifacts;

  @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ArchivedArtifact> archivedArtifacts;

  private Boolean doc;
  private boolean isInvalidArtifact;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Artifact that = (Artifact) o;
    return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId);
  }

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}
