package com.axonivy.market.entity.productjsonfilecontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
  private String groupId;
  private String artifactId;
  private String version;
  private String type;
}
