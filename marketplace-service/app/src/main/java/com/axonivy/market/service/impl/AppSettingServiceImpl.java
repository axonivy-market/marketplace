package com.axonivy.market.service.impl;

import com.axonivy.market.entity.AppSetting;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;
import com.axonivy.market.repository.AppSettingRepository;
import com.axonivy.market.service.AppSettingService;
import com.axonivy.market.service.EncryptionService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

  private final AppSettingRepository repository;
  private final EncryptionService encryptionService;

  public List<AppSettingDto> findAll() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  public List<AppSettingDto> search(String keyword) {

    if (StringUtils.isBlank(keyword)) {
      return findAll();
    }

    return repository.findByKeyContainingIgnoreCase(keyword).stream().map(this::toDto).toList();
  }

  @Override
  public AppSettingDto update(String key, String value) {
    AppSetting setting = repository.findByKey(key).orElseThrow(
        () -> new EntityNotFoundException("Setting not found for key: " + key));

    if (setting.getEncrypted()) {
      setting.setValue(encryptionService.encrypt(value));
    } else {
      setting.setValue(value);
    }

    repository.save(setting);
    return toDto(setting);
  }

  @Override
  public String getValueByKey(AppSettingKey setting) {
    return repository.findByKey(setting.getKey()).map(this::resolveValue).filter(StringUtils::isNotBlank).orElse(
        setting.getDefaultValue());
  }

  public String getValue(String key) {
    return repository.findById(key).map(this::resolveValue).orElse(null);
  }

  private AppSettingDto toDto(AppSetting entity) {
    return AppSettingDto.from(entity, resolveValue(entity));
  }

  private String resolveValue(AppSetting setting) {
    String value = setting.getValue().trim();
    if (StringUtils.isBlank(value) || !setting.getEncrypted()) {
      return value;
    }
    return encryptionService.decrypt(value).trim();
  }

  @PostConstruct
  @Transactional
  public void initializeSettings() {
    Set<String> enumKeys = Arrays.stream(AppSettingKey.values()).map(AppSettingKey::getKey).collect(Collectors.toSet());

    Set<String> existingKeys = repository.findAllKeys();

    repository.saveAll(
        Arrays.stream(AppSettingKey.values()).filter(settingKey -> !existingKeys.contains(settingKey.getKey())).map(
            AppSetting::from).toList());

    repository.deleteByKeyNotIn(enumKeys);
  }
}