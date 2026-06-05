package com.axonivy.market.service.impl;

import com.axonivy.market.entity.AppSetting;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;
import com.axonivy.market.repository.AppSettingRepository;
import com.axonivy.market.service.EncryptionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingServiceImplTest {

  @Mock
  private AppSettingRepository repository;

  @Mock
  private EncryptionService encryptionService;

  @InjectMocks
  private AppSettingServiceImpl appSettingService;

  @Test
  void testFindAllReturnsAllSettings() {
    AppSetting setting1 = buildAppSetting("key1", "value1", false);
    AppSetting setting2 = buildAppSetting("key2", "value2", false);
    when(repository.findAll()).thenReturn(List.of(setting1, setting2));

    List<AppSettingDto> result = appSettingService.findAll();

    assertEquals(2, result.size(), "Expected two settings");
    assertEquals("key1", result.get(0).getSettingKey(), "First setting key should match");
    assertEquals("value1", result.get(0).getSettingValue(), "First setting value should match");
    assertEquals("key2", result.get(1).getSettingKey(), "Second setting key should match");
    assertEquals("value2", result.get(1).getSettingValue(), "Second setting value should match");
  }

  @Test
  void testFindAllDecryptsEncryptedSettings() {
    AppSetting encryptedSetting = buildAppSetting("secret-key", "encrypted-value", true);
    when(repository.findAll()).thenReturn(List.of(encryptedSetting));
    when(encryptionService.decrypt("encrypted-value")).thenReturn("decrypted-value");

    List<AppSettingDto> result = appSettingService.findAll();

    assertEquals(1, result.size(), "Expected one setting");
    assertEquals("decrypted-value", result.getFirst().getSettingValue(),
        "Encrypted setting value should be decrypted in DTO");
  }

  @Test
  void testSearchWithBlankKeywordReturnsFindAll() {
    AppSetting setting = buildAppSetting("key1", "value1", false);
    when(repository.findAll()).thenReturn(List.of(setting));

    List<AppSettingDto> result = appSettingService.search("");

    assertEquals(1, result.size(), "Expected one setting when searching with blank keyword");
    verify(repository).findAll();
    verify(repository, never()).findByKeyContainingIgnoreCase(anyString());
  }

  @Test
  void testSearchWithNullKeywordReturnsFindAll() {
    when(repository.findAll()).thenReturn(List.of());

    List<AppSettingDto> result = appSettingService.search(null);

    assertTrue(result.isEmpty(), "Expected empty result when searching with null keyword");
    verify(repository).findAll();
  }

  @Test
  void testSearchWithKeywordFiltersResults() {
    AppSetting setting = buildAppSetting("market.github.token", "token-value", false);
    when(repository.findByKeyContainingIgnoreCase("github")).thenReturn(List.of(setting));

    List<AppSettingDto> result = appSettingService.search("github");

    assertEquals(1, result.size(), "Expected one setting matching the keyword");
    assertEquals("market.github.token", result.getFirst().getSettingKey(),
        "Setting key should match the search keyword");
    verify(repository).findByKeyContainingIgnoreCase("github");
  }

  @Test
  void testUpdateNonEncryptedSetting() {
    AppSetting setting = buildAppSetting("key1", "old-value", false);
    when(repository.findByKey("key1")).thenReturn(Optional.of(setting));

    AppSettingDto result = appSettingService.update("key1", "new-value");

    assertEquals("new-value", result.getSettingValue(), "Setting value should be updated");
    verify(repository).save(setting);
    verify(encryptionService, never()).encrypt(anyString());
  }

  @Test
  void testUpdateEncryptedSetting() {
    AppSetting setting = buildAppSetting("secret-key", "old-encrypted", true);
    when(repository.findByKey("secret-key")).thenReturn(Optional.of(setting));
    when(encryptionService.encrypt("new-plain-value")).thenReturn("new-encrypted-value");
    when(encryptionService.decrypt("new-encrypted-value")).thenReturn("new-plain-value");

    AppSettingDto result = appSettingService.update("secret-key", "new-plain-value");

    assertEquals("new-plain-value", result.getSettingValue(),
        "Encrypted setting should be decrypted in the returned DTO");
    verify(encryptionService).encrypt("new-plain-value");
    verify(repository).save(setting);
  }

  @Test
  void testUpdateThrowsWhenSettingNotFound() {
    when(repository.findByKey("nonexistent")).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class,
        () -> appSettingService.update("nonexistent", "value"),
        "Expected EntityNotFoundException when setting key does not exist");
  }

  @Test
  void testGetValueByKeyReturnsStoredValue() {
    AppSetting setting = buildAppSetting(AppSettingKey.GITHUB_TOKEN.getKey(), "stored-token", false);
    when(repository.findByKey(AppSettingKey.GITHUB_TOKEN.getKey())).thenReturn(Optional.of(setting));

    String value = appSettingService.getValueByKey(AppSettingKey.GITHUB_TOKEN);

    assertEquals("stored-token", value, "Should return stored value from repository");
  }

  @Test
  void testGetValueByKeyReturnsDecryptedValueForEncryptedSetting() {
    AppSetting setting = buildAppSetting(AppSettingKey.GITHUB_TOKEN.getKey(), "encrypted-token", true);
    when(repository.findByKey(AppSettingKey.GITHUB_TOKEN.getKey())).thenReturn(Optional.of(setting));
    when(encryptionService.decrypt("encrypted-token")).thenReturn("decrypted-token");

    String value = appSettingService.getValueByKey(AppSettingKey.GITHUB_TOKEN);

    assertEquals("decrypted-token", value, "Should return decrypted value for encrypted settings");
  }

  @Test
  void testGetValueByKeyReturnsDefaultWhenNotFound() {
    when(repository.findByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT.getKey())).thenReturn(Optional.empty());

    String value = appSettingService.getValueByKey(AppSettingKey.GITHUB_CONNECT_TIMEOUT);

    assertEquals(AppSettingKey.GITHUB_CONNECT_TIMEOUT.getDefaultValue(), value,
        "Should return default value when setting is not found in repository");
  }

  @Test
  void testGetValueByKeyReturnsDefaultWhenStoredValueIsBlank() {
    AppSetting setting = buildAppSetting(AppSettingKey.GITHUB_TOKEN.getKey(), "", false);
    when(repository.findByKey(AppSettingKey.GITHUB_TOKEN.getKey())).thenReturn(Optional.of(setting));

    String value = appSettingService.getValueByKey(AppSettingKey.GITHUB_TOKEN);

    assertEquals(AppSettingKey.GITHUB_TOKEN.getDefaultValue(), value,
        "Should return default value when stored value is blank");
  }

  @Test
  void testGetValueReturnsResolvedValue() {
    AppSetting setting = buildAppSetting("custom-key", "custom-value", false);
    when(repository.findById("custom-key")).thenReturn(Optional.of(setting));

    String result = appSettingService.getValue("custom-key");

    assertEquals("custom-value", result, "Should return the value from repository for the given key");
  }

  @Test
  void testGetValueReturnsNullWhenNotFound() {
    when(repository.findById("missing-key")).thenReturn(Optional.empty());

    String result = appSettingService.getValue("missing-key");

    assertNull(result, "Should return null when key is not found");
  }

  @Test
  void testInitializeSettingsCreatesNewAndDeletesObsolete() {
    Set<String> existingKeys = Set.of(AppSettingKey.GITHUB_TOKEN.getKey());
    when(repository.findAllKeys()).thenReturn(existingKeys);

    appSettingService.initializeSettings();

    ArgumentCaptor<List<AppSetting>> captor = ArgumentCaptor.forClass(List.class);
    verify(repository).saveAll(captor.capture());

    List<AppSetting> savedSettings = captor.getValue();
    assertTrue(savedSettings.stream().noneMatch(s -> s.getKey().equals(AppSettingKey.GITHUB_TOKEN.getKey())),
        "Should not save settings that already exist");
    assertFalse(savedSettings.isEmpty(),
        "Should save new settings that don't exist yet");

    verify(repository).deleteByKeyNotIn(anySet());
  }

  @Test
  void testInitializeSettingsWithEmptyExistingKeys() {
    when(repository.findAllKeys()).thenReturn(Set.of());

    appSettingService.initializeSettings();

    ArgumentCaptor<List<AppSetting>> captor = ArgumentCaptor.forClass(List.class);
    verify(repository).saveAll(captor.capture());

    List<AppSetting> savedSettings = captor.getValue();
    assertEquals(AppSettingKey.values().length, savedSettings.size(),
        "Should save all enum keys when no keys exist in repository");
  }

  private AppSetting buildAppSetting(String key, String value, boolean encrypted) {
    return AppSetting.builder()
        .key(key)
        .value(value)
        .category("TEST")
        .description("Test setting")
        .encrypted(encrypted)
        .build();
  }
}




