package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.util.Objects;
import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.ARTIFACT;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = ARTIFACT)
public class Artifact extends GenericIdEntity {
  private String repoUrl;
  private String name;
  private String groupId;
  private String artifactId;
  private String type;
  private Boolean isDependency;
  @Transient
  private Boolean isProductArtifact;
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private Set<ArchivedArtifact> archivedArtifacts;
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
}
