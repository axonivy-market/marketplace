package com.axonivy.market.core.repository;

import com.axonivy.market.core.CoreTestApplication;
import com.axonivy.market.core.entity.AppSetting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CoreTestApplication.class)
@ActiveProfiles("test")
@Transactional
class AppSettingRepositoryTest {

  private static final String GITHUB = "github";
  private static final String GITHUB_TOKEN_KEY = "market.github.token";
  private static final String GITHUB_CLIENT_ID_KEY = "market.github.client-id";
  private static final String SECURITY_ENABLED_KEY = "market.security.enabled";
  private static final String CLIENT_ID_SEARCH_TEXT = "client-id";
  private static final String GITHUB_KEYWORD = "GITHUB";

  @Autowired
  private AppSettingRepository repository;

  @Test
  void shouldFindSettingsByKeyIgnoringCase() {
    var result = repository.findByKeyContainingIgnoreCase(CLIENT_ID_SEARCH_TEXT);

    assertThat(result)
        .extracting(AppSetting::getKey)
        .containsExactly(GITHUB_CLIENT_ID_KEY);
  }

  @Test
  void shouldFindSettingsByCategoryIgnoringCaseAndReturnAllKeys() {
    var githubSettings = repository.findByCategoryIgnoreCase(GITHUB);
    var keys = repository.findAllKeys();

    assertThat(githubSettings)
        .extracting(AppSetting::getKey)
        .containsExactlyInAnyOrder(GITHUB_TOKEN_KEY, GITHUB_CLIENT_ID_KEY);
    assertThat(keys)
        .containsExactlyInAnyOrder(GITHUB_TOKEN_KEY, GITHUB_CLIENT_ID_KEY, SECURITY_ENABLED_KEY);
  }

  @Test
  void shouldDeleteSettingsNotIncludedInKeySet() {
    repository.deleteByKeyNotIn(Set.of(GITHUB_TOKEN_KEY));

    assertThat(repository.findAll())
        .extracting(AppSetting::getKey)
        .containsExactly(GITHUB_TOKEN_KEY);
  }
}
