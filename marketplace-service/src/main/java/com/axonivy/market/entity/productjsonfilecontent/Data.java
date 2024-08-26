package com.axonivy.market.entity.productjsonfilecontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Data {
  private List<Project> projects;
  private List<Repository> repositories;
  private List<Dependency> dependencies;
}
