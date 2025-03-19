package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Objects;

import static com.axonivy.market.constants.EntityConstants.ARTIFACT;
import static com.axonivy.market.constants.EntityConstants.PRODUCT_ID;

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

  @ManyToOne
  @JoinColumn(name = PRODUCT_ID, nullable = false)
  @JsonBackReference
  private Product product;

  @OneToMany(mappedBy = ARTIFACT, cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JsonManagedReference
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
}
