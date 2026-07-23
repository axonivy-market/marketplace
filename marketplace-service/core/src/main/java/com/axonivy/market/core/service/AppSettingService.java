package com.axonivy.market.core.service;

import com.axonivy.market.core.enums.AppSettingCategory;
import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.model.AppSettingDto;


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
