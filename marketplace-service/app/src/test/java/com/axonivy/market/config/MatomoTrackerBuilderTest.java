package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingCategory;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matomo.java.tracking.MatomoTracker;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatomoTrackerBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private MatomoTrackerBuilder builder;

  private final Map<String, String> matomoSettings = Map.ofEntries(
      Map.entry(AppSettingKey.MATOMO_SITE_ID.getKey(), "1234"),
      Map.entry(AppSettingKey.MATOMO_API_ENDPOINT.getKey(), "https://matomo.example.com/matomo.php"),
      Map.entry(AppSettingKey.MATOMO_ENABLED.getKey(), "false"));

  @BeforeEach
  void setUp() {
    builder = new MatomoTrackerBuilder(appSettingService);
  }

  @Test
  void testBuildCreatesMatomoTracker() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(matomoSettings);
    MatomoTracker tracker = builder.build();
    assertNotNull(tracker, "MatomoTracker should not be null");
  }

  @Test
  void testBuildReturnsCachedTrackerWhenSettingsUnchanged() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(matomoSettings);
    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertSame(first, second, "Should return cached tracker when settings have not changed");
  }

  @Test
  void testBuildCreatesNewTrackerWhenSiteIdChanges() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(
        matomoSettings("1234", "https://matomo.example.com/matomo.php", "false")).thenReturn(
        matomoSettings("789", "https://matomo.example.com/matomo.php", "false"));

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when site ID changes");
  }

  @Test
  void testBuildCreatesNewTrackerWhenEnabledChanges() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(
        matomoSettings("1234", "https://matomo.example.com/matomo.php", "false")).thenReturn(
        matomoSettings("1234", "https://matomo.example.com/matomo.php", "true"));

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when enabled setting changes");
  }

  @Test
  void testBuildReturnsPreviousTrackerWhenEndpointChanges() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(
        matomoSettings("1234", "https://matomo.example.com/matomo.php", "false")).thenReturn(
        matomoSettings("1234", "invalid-uri", "false"));

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should return previous tracker when endpoint is invalid");
  }

  @Test
  void testBuildDisabledTracker() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(matomoSettings);
    MatomoTracker tracker = builder.build();
    assertNotNull(tracker, "Should create tracker even when disabled");
  }

  private Map<String, String> matomoSettings(String siteId, String endpoint, String enabled) {
    return Map.of(AppSettingKey.MATOMO_SITE_ID.getKey(), siteId, AppSettingKey.MATOMO_API_ENDPOINT.getKey(), endpoint,
        AppSettingKey.MATOMO_ENABLED.getKey(), enabled);
  }
}