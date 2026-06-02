package com.axonivy.market.service.impl;

import com.axonivy.market.entity.AppSetting;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;
import com.axonivy.market.repository.AppSettingRepository;
import com.axonivy.market.service.AppSettingService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

  private final AppSettingRepository repository;

  public List<AppSettingDto> findAll() {
    return repository.findAll()
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  public List<AppSettingDto> search(String keyword) {

    if (keyword == null || keyword.isBlank()) {
      return findAll();
    }

    return repository.findByKeyContainingIgnoreCase(keyword)
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  public AppSettingDto update(String key, String value) {

    AppSetting setting = repository.findByKey(key)
        .orElseThrow(() ->
            new EntityNotFoundException(
                "Setting not found: " + key));

    setting.setValue(value);
    repository.save(setting);
    return toDto(setting);
  }

  @Override
  public String getValueByKey(AppSettingKey setting) {
    return repository.findByKey(setting.getKey())
        .map(AppSetting::getValue)
        .filter(StringUtils::isNotBlank)
        .orElse(setting.getDefaultValue());
  }

  public String getValue(String key) {

    return repository.findById(key)
        .map(AppSetting::getValue)
        .orElse(null);
  }

  private AppSettingDto toDto(AppSetting entity) {

    AppSettingDto dto = new AppSettingDto();

    dto.setSettingKey(entity.getKey());

    dto.setSettingValue(
        entity.getEncrypted()
            ? "********"
            : entity.getValue());

    dto.setCategory(entity.getCategory());

    dto.setDescription(entity.getDescription());

    dto.setEncrypted(entity.getEncrypted());

    return dto;
  }

  @PostConstruct
  @Transactional
  public void initializeSettings() {
    Set<String> existingKeys = repository.findAllKeys();
    List<AppSetting> newSettings = Arrays.stream(AppSettingKey.values())
        .filter(settingKey -> !existingKeys.contains(settingKey.getKey()))
        .map(AppSetting::from)
        .toList();
    if (!newSettings.isEmpty()) {
      repository.saveAll(newSettings);
    }
  }
}