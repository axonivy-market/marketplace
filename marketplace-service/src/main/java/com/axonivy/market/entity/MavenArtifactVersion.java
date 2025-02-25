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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @OneToMany(mappedBy = "productVersionReference", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JsonManagedReference("productVersionReference")
  private List<MavenArtifactModel> productArtifactsByVersionTest;

  @OneToMany(mappedBy = "additionalVersionReference", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JsonManagedReference("additionalVersionReference")
  private List<MavenArtifactModel> additionalArtifactsByVersionTest;;

  public List<MavenArtifactModel> getProductArtifactsByVersionTest() {
    if (Objects.isNull(productArtifactsByVersionTest)) {
      productArtifactsByVersionTest = new ArrayList<>();
    }
    return productArtifactsByVersionTest;
  }

  public List<MavenArtifactModel> getAdditionalArtifactsByVersionTest() {
    if (Objects.isNull(additionalArtifactsByVersionTest)) {
      additionalArtifactsByVersionTest = new ArrayList<>();
    }
    return additionalArtifactsByVersionTest;
  }
}
