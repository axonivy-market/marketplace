package com.axonivy.market.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenArtifact {
    private String repoUrl;
    private String name;
    private String groupId;
    private String artifactId;
    private String type;
    private Boolean isDependency;
    private List<ArchivedArtifact> archivedArtifacts;

    public MavenArtifact(String repoUrl, String artifactId, String groupId, String artifactId1, String type) {
    }
}
