package com.axonivy.market.entity.key;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
public class MavenArtifactKey implements Serializable {
  @Serial
  private static final long serialVersionUID = 1;

  private String artifactId;
  private String productVersion;
  private boolean isAdditionalVersion;
}
