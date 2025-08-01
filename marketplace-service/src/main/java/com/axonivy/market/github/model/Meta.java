package com.axonivy.market.github.model;

import com.axonivy.market.entity.Artifact;
import com.axonivy.market.model.DisplayValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Meta {
  @JsonProperty("$schema")
  private String schema;
  private String id;
  private List<DisplayValue> names;
  private List<DisplayValue> descriptions;
  private String type;
  private String platformReview;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private Boolean listed;
  private String version;
  private String vendor;
  private String vendorImage;
  private String vendorImageDarkMode;
  private String vendorUrl;
  private List<String> tags;
  private List<Artifact> mavenArtifacts;
  private Boolean contactUs;
  private String cost;
  private Boolean deprecated;
}
