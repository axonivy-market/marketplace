package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.axonivy.market.constants.EntityConstants.MAVEN_DEPENDENCY;
import static com.axonivy.market.constants.EntityConstants.PARENT_DEPENDENCY_ID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = MAVEN_DEPENDENCY)
public class MavenDependency {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String mavenName;
  private String artifactId;
  private String version;
  private String downloadUrl;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = PARENT_DEPENDENCY_ID)
  private List<MavenDependency> dependencies;
}
