package com.axonivy.market.github.model;

import java.util.List;

import com.axonivy.market.model.DisplayValue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
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
  private String vendorUrl;
  private List<String> tags;
  private List<MavenArtifact> mavenArtifacts;
  private String compatibility;
  private Boolean contactUs;
  private String cost;
}
