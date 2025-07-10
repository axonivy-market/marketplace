package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repository")
public class Repository extends AuditableIdEntity{

  @Id
  private String id;
  private String name;
  private String htmlUrl;
  private boolean archived;
  private boolean isTemplate;
  private String defaultBranch;
  private String language;
  private LocalDateTime lastUpdated;

  @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkflowRepository> workflows;
}
