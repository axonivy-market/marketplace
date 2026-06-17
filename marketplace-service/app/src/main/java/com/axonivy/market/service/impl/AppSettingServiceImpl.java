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
  public List<AppSettingDto> getAllByCategory(String category) {
    return List.of();
  }

  @Override
  public String getStringValueByKey(AppSettingKey setting) {
    return repository.findByKey(setting.getKey()).map(this::resolveValue).filter(StringUtils::isNotBlank).orElse(
        setting.getDefaultValue());
  }

  private AppSettingDto toDto(AppSetting entity) {
    return AppSettingDto.from(entity, resolveValue(entity));
  }

  private String resolveValue(AppSetting setting) {
    String value = StringUtils.trimToEmpty(setting.getValue());
    boolean encrypted = Boolean.TRUE.equals(setting.getEncrypted());
    if (StringUtils.isBlank(value) || !encrypted) {
      return value;
    }
    return StringUtils.trimToEmpty(encryptionService.decrypt(value));
  }

  @Override
  public Long getLongValueByKey(AppSettingKey setting) {
    var value = getStringValueByKey(setting);
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      try {
        return Long.parseLong(setting.getDefaultValue());
      } catch (NumberFormatException defaultEx) {
        log.warn("Invalid long value '{}' and invalid default '{}' for setting key '{}'.", value,
            setting.getDefaultValue(), setting.getKey(), defaultEx);
        return 0L;
      }
    }
  }

  @Override
  public Integer getIntegerValueByKey(AppSettingKey setting) {
    var value = getStringValueByKey(setting);

    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      try {
        return Integer.parseInt(setting.getDefaultValue());
      } catch (NumberFormatException defaultEx) {
        log.warn("Invalid integer value '{}' and invalid default '{}' for setting key '{}'", value,
            setting.getDefaultValue(), setting.getKey(), defaultEx);
        return null;
      }
    }
  }

  @Override
  public Boolean getBooleanValueByKey(AppSettingKey setting) {
    var value = getStringValueByKey(setting);
    return Boolean.valueOf(value);
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