package com.axonivy.market.service;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;

import java.util.List;

public interface AppSettingService {

  List<AppSettingDto> search(String search);

  AppSettingDto update(String key, String value);

  Long getLongValueByKey(AppSettingKey key);

  Boolean getBooleanValueByKey(AppSettingKey key);

  Integer getIntegerValueByKey(AppSettingKey key);

  String getStringValueByKey(AppSettingKey key);

}
