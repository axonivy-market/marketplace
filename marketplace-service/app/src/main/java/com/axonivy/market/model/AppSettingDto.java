package com.axonivy.market.model;

import com.axonivy.market.entity.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingDto {

  private String settingKey;

  private String settingValue;

  private String category;

  private String description;

  private boolean encrypted;

  public static AppSettingDto from(AppSetting setting, String value) {
    return AppSettingDto.builder()
        .settingKey(setting.getKey())
        .settingValue(value)
        .category(setting.getCategory())
        .description(setting.getDescription())
        .encrypted(setting.getEncrypted())
        .build();
  }
}