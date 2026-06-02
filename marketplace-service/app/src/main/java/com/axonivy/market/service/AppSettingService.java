package com.axonivy.market.service;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;

import java.util.List;

public interface AppSettingService {

  List<AppSettingDto> search(String search);

  AppSettingDto update(String key, String value);

  String getValueByKey(AppSettingKey key);

}
