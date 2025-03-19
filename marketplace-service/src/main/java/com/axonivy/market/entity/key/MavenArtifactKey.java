package com.axonivy.market.entity.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class MavenArtifactKey implements Serializable {
  private String artifactId;
  private String productVersion;
  private boolean isAdditionalVersion;
}
