package com.axonivy.market.entity;

import com.axonivy.market.model.MavenArtifactModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.axonivy.market.constants.EntityConstants.MAVEN_ARTIFACT_VERSION;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(MAVEN_ARTIFACT_VERSION)
public class MavenArtifactVersion implements Serializable {
  @Serial
  private static final long serialVersionUID = -6492612804634492078L;
  @Id
  private String productId;
  private Map<String, List<MavenArtifactModel>> productArtifactsByVersion;
  private Map<String, List<MavenArtifactModel>> additionalArtifactsByVersion;

  public Map<String, List<MavenArtifactModel>> getProductArtifactsByVersion() {
    if (Objects.isNull(productArtifactsByVersion)) {
      this.productArtifactsByVersion = new HashMap<>();
    }
    return this.productArtifactsByVersion;
  }

  public Map<String, List<MavenArtifactModel>> getAdditionalArtifactsByVersion() {
    if (Objects.isNull(this.additionalArtifactsByVersion)) {
      this.additionalArtifactsByVersion = new HashMap<>();
    }
    return this.additionalArtifactsByVersion;
  }
}
