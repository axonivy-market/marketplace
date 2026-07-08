package com.axonivy.market.controller;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.AppSettingDto;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingControllerTest {

  private AppSettingController controller;

  @Mock
  private AppSettingService service;

  @BeforeEach
  void setUp() {
    controller = new AppSettingController(service);
  }

  @Test
  void testGetSettingsWithNoSearch() {
    List<AppSettingDto> settings = List.of(
        buildDto(AppSettingKey.GITHUB_TOKEN, "token-value"),
        buildDto(AppSettingKey.GITHUB_CONNECT_TIMEOUT, "10000"));
    when(service.search(null)).thenReturn(settings);

    ResponseEntity<List<AppSettingDto>> response = controller.getSettings(null);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(2, response.getBody().size(), "Should return all settings");
    verify(service, times(1)).search(null);
  }

  @Test
  void testGetSettingsWithSearchKeyword() {
    List<AppSettingDto> filtered = List.of(buildDto(AppSettingKey.GITHUB_TOKEN, "token-value"));
    when(service.search("github")).thenReturn(filtered);

    ResponseEntity<List<AppSettingDto>> response = controller.getSettings("github");

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(1, response.getBody().size(), "Should return filtered settings");
    assertEquals(AppSettingKey.GITHUB_TOKEN.getKey(), response.getBody().getFirst().getSettingKey(),
        "Returned setting key should match the search");
    verify(service, times(1)).search("github");
  }

  @Test
  void testGetSettingsReturnsEmptyList() {
    when(service.search("nonexistent")).thenReturn(Collections.emptyList());

    ResponseEntity<List<AppSettingDto>> response = controller.getSettings("nonexistent");

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertTrue(response.getBody().isEmpty(), "Should return empty list when no settings match");
  }

  @Test
  void testUpdateSetting() {
    AppSettingDto request = buildDto(AppSettingKey.GITHUB_TOKEN, "new-token");
    AppSettingDto updated = buildDto(AppSettingKey.GITHUB_TOKEN, "new-token");
    when(service.update(AppSettingKey.GITHUB_TOKEN.getKey(), "new-token")).thenReturn(updated);

    ResponseEntity<AppSettingDto> response = controller.updateSetting(AppSettingKey.GITHUB_TOKEN.getKey(), request);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(AppSettingKey.GITHUB_TOKEN.getKey(), response.getBody().getSettingKey(), "Returned setting key should match");
    assertEquals("new-token", response.getBody().getSettingValue(), "Returned setting value should be updated");
    verify(service, times(1)).update(AppSettingKey.GITHUB_TOKEN.getKey(), "new-token");
  }

  @Test
  void testUpdateSettingPassesCorrectValueFromRequestBody() {
    AppSettingDto request = buildDto(AppSettingKey.MAIL_PORT, "465");
    AppSettingDto updated = buildDto(AppSettingKey.MAIL_PORT, "465");
    when(service.update(AppSettingKey.MAIL_PORT.getKey(), "465")).thenReturn(updated);

    ResponseEntity<AppSettingDto> response = controller.updateSetting(AppSettingKey.MAIL_PORT.getKey(), request);

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals("465", response.getBody().getSettingValue(),
        "Should pass the settingValue from the request body to the service");
    verify(service).update(AppSettingKey.MAIL_PORT.getKey(), "465");
  }

  @Test
  void testGetSettingsWithEmptySearch() {
    List<AppSettingDto> allSettings = List.of(buildDto(AppSettingKey.PRODUCTS_CRON, "0 30 * * * *"));
    when(service.search("")).thenReturn(allSettings);

    ResponseEntity<List<AppSettingDto>> response = controller.getSettings("");

    assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");
    assertNotNull(response.getBody(), "Response body should not be null");
    assertEquals(1, response.getBody().size(), "Should delegate empty search to service");
    verify(service, times(1)).search("");
  }

  private AppSettingDto buildDto(AppSettingKey settingKey, String value) {
    return AppSettingDto.builder()
        .settingKey(settingKey.getKey())
        .settingValue(value)
        .category(settingKey.getCategory())
        .description(settingKey.getDescription())
        .encrypted(settingKey.isEncrypted())
        .build();
  }
}
