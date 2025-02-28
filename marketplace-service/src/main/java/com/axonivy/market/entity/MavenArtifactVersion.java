package com.axonivy.market.entity;

import com.axonivy.market.model.MavenArtifactModel;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.axonivy.market.constants.EntityConstants.MAVEN_ARTIFACT_VERSION;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = MAVEN_ARTIFACT_VERSION)
public class MavenArtifactVersion implements Serializable {
  @Serial
  private static final long serialVersionUID = -6492612804634492078L;
  @Id
  private String productId;

  @OneToMany(mappedBy = "productVersionReference", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JsonManagedReference("productVersionReference")
  private List<MavenArtifactModel> productArtifactsByVersion;

  @OneToMany(mappedBy = "additionalVersionReference", cascade = CascadeType.ALL, fetch = FetchType.EAGER ,orphanRemoval = true)
  @JsonManagedReference("additionalVersionReference")
  private List<MavenArtifactModel> additionalArtifactsByVersion;;

  public List<MavenArtifactModel> getProductArtifactsByVersion() {
    if (Objects.isNull(productArtifactsByVersion)) {
      productArtifactsByVersion = new ArrayList<>();
    }
    return productArtifactsByVersion;
  }

  public List<MavenArtifactModel> getAdditionalArtifactsByVersion() {
    if (Objects.isNull(additionalArtifactsByVersion)) {
      additionalArtifactsByVersion = new ArrayList<>();
    }
    return additionalArtifactsByVersion;
  }
}
