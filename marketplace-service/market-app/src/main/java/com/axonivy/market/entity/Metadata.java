package com.axonivy.market.entity;

import com.axonivy.market.converter.StringSetConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.METADATA;
import static com.axonivy.market.constants.EntityConstants.TEXT_TYPE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = METADATA)
public class Metadata extends AbstractGenericEntity<String> {

  @Serial
  private static final long serialVersionUID = 1;

  @Id
  private String url;
  private String productId;
  private LocalDateTime lastUpdated;
  private String artifactId;
  private String groupId;
  private String latest;
  private String release;

  @Convert(converter = StringSetConverter.class)
  @Column(nullable = false, columnDefinition = TEXT_TYPE)
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

  @Override
  public String getId() {
    return url;
  }

  @Override
  public void setId(String url) {
    this.url = url;
  }
}
