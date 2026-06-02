package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingDto {

  private String settingKey;

  private String settingValue;

  private String category;

  private String description;

  private boolean encrypted;
}