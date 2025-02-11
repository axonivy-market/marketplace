package com.axonivy.market.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MavenDependency {
  private String artifactId;
  private String version;
  private String downloadUrl;
  private List<MavenDependency> dependencies;
}
