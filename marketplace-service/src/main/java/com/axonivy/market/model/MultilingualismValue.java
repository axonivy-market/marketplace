package com.axonivy.market.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class MultilingualismValue implements Serializable {
  private static final long serialVersionUID = -4193508237020296419L;

  private String en;
  private String de;
}
