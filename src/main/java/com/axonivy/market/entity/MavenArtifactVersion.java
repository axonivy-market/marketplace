package com.axonivy.market.entity;

import com.axonivy.market.github.model.MavenArtifactModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.MAVEN_ARTIFACT_VERSION;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(MAVEN_ARTIFACT_VERSION)
public class MavenArtifactVersion implements Serializable {
    @Id
    private String productKey;
    private List<String> versions;
    private Map<String, List<MavenArtifactModel>> artifactVersioned;
}
