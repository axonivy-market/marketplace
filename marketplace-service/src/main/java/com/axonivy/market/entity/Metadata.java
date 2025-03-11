package com.axonivy.market.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = METADATA)
public class Metadata implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Id
  private String url;
  private String productId;
  private LocalDateTime lastUpdated;
  private String artifactId;
  private String groupId;
  private String latest;
  private String release;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = METADATA_VERSIONS, joinColumns = @JoinColumn(name = PRODUCT_URL))
  @Column
  private Set<String> versions;

  private String repoUrl;
  private String type;
  private String name;
  private boolean isProductArtifact;
  private String snapshotVersionValue;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata that = (Metadata) o;
    return Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url);
  }
}
