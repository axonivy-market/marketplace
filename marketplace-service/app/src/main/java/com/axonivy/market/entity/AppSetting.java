package com.axonivy.market.entity;

import com.axonivy.market.core.entity.GenericIdEntity;
import com.axonivy.market.enums.AppSettingKey;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@Builder
@Entity
@Table(name = "app_settings")
@AllArgsConstructor
@NoArgsConstructor
public class AppSetting extends GenericIdEntity {

  @Serial
  private static final long serialVersionUID = 1L;

  private String key;
  private String value;
  private String category;
  private String description;
  private Boolean encrypted;

  public static AppSetting from(AppSettingKey key) {
    return AppSetting.builder()
        .key(key.getKey())
        .value(key.getDefaultValue())
        .category(key.getCategory())
        .description(key.getDescription())
        .encrypted(key.isEncrypted())
        .build();
  }
}