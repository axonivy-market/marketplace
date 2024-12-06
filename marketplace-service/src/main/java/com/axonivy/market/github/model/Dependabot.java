package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Dependabot {
  private Map<String, Integer> alerts;
  private AccessLevel status;
}
