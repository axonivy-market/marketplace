package com.axonivy.market.bo;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MavenVersion {
  @Id
  private String metadataUrl;
  private String artifactId;
  private String groupId;
  private String latest;
  private String release;
  private LocalDateTime lastUpdated;
  private List<String> versions;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MavenVersion that = (MavenVersion) o;
    return Objects.equals(metadataUrl, that.metadataUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(metadataUrl);
  }
}
