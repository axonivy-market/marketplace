package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailArtifactModel {
    private Map<String, List<MavenArtifactModel>> artifact;
}
