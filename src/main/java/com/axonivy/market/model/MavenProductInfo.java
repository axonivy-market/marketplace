package com.axonivy.market.model;

import java.util.List;

import com.axonivy.market.github.model.MavenArtifact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MavenProductInfo {
  private MavenArtifact productMavenArtifact;
  private List<MavenArtifact> additionalMavenArtifacts;
}