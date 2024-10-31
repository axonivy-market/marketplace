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
public class IntroductionDataModel {
  Map<String, String> description;
  Map<String, String> demo;
  Map<String, String> setup;
}
