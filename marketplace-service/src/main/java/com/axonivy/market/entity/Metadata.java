package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static com.axonivy.market.constants.EntityConstants.METADATA;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(METADATA)
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
