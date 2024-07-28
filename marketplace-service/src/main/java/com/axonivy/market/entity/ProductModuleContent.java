package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductModuleContent implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private String tag;
  private Map<String, String> description;
  private String setup;
  private String demo;
  private Boolean isDependency;
  private String name;
  private String groupId;
  private String artifactId;
  private String type;
}
