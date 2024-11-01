package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReadmeContentsModel {
  private String description;
  private String demo;
  private String setup;
}
