package com.axonivy.market.controller;

import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.model.AppSettingDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(AppSettingController.class)
class AppSettingControllerTest extends WebMvcControllerTestSupport {

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @Test
  void testGetSettings() throws Exception {
    List<AppSettingDto> settings = List.of(
        buildDto(AppSettingKey.GITHUB_TOKEN, "token-value"),
        buildDto(AppSettingKey.GITHUB_CONNECT_TIMEOUT, "10000"));
    when(appSettingService.search("github")).thenReturn(settings);

    mockMvc.perform(get("/api/settings").param("search", "github"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..settingKey").value(hasItem(AppSettingKey.GITHUB_TOKEN.getKey())))
        .andExpect(jsonPath("$..settingKey").value(hasItem(AppSettingKey.GITHUB_CONNECT_TIMEOUT.getKey())));
  }

  @Test
  void testUpdateSetting() throws Exception {
    AppSettingDto request = buildDto(AppSettingKey.MAIL_PORT, "465");
    when(appSettingService.update(AppSettingKey.MAIL_PORT.getKey(), "465")).thenReturn(request);

    mockMvc.perform(put("/api/settings/{key}", AppSettingKey.MAIL_PORT.getKey())
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settingKey").value(AppSettingKey.MAIL_PORT.getKey()))
        .andExpect(jsonPath("$.settingValue").value("465"));
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
