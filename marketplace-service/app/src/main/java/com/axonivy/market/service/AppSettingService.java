package com.axonivy.market.service;

import com.axonivy.market.enums.AppSettingCategory;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;

import java.util.List;
import java.util.Map;

public interface AppSettingService {

  List<AppSettingDto> search(String search);

  AppSettingDto update(String key, String value);

  Map<String, String> getByCategory(AppSettingCategory category);

  Long getLongValueByKey(AppSettingKey key);

  Boolean getBooleanValueByKey(AppSettingKey key);

  Integer getIntegerValueByKey(AppSettingKey key);

  String getStringValueByKey(AppSettingKey key);

}
