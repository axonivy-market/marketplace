package com.axonivy.market.entity;

import com.axonivy.market.model.MavenArtifactModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.MAVEN_ARTIFACT_VERSION;

@Getter
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
}