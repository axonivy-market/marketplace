package com.axonivy.market.bo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "maven_dependency")
public class MavenDependency {
  @Id
  private String id;
  private String mavenName;
  private String artifactId;
  private String version;
  private String downloadUrl;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "parent_dependency_id") // Foreign key in MavenDependency table
  private List<MavenDependency> dependencies;

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}
