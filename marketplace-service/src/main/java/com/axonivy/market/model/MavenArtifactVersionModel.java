package com.axonivy.market.model;

import com.axonivy.market.entity.MavenArtifactModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MavenArtifactVersionModel {
	private String version;
	private List<MavenArtifactModel> artifactsByVersion;
}