package com.axonivy.market.entity.productjsonfilecontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dependency {
  private String artifactId;
  private String classifier;
  private List<Exclusion> exclusions;
  private String groupId;
  private String managementKey;
  private String optional;
  private String scope;
  private String systemPath;
  private String type;
  private String version;
}
