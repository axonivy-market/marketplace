package com.axonivy.market.stable.config;

import com.axonivy.market.core.config.MatomoTrackerBuilder;
import com.axonivy.market.core.enums.AppSettingCategory;
import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
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
class MatomoStableTrackerBuilderTest {
  private static final String SITE_ID = "1234";
  private static final String API_ENDPOINT = "https://matomo.example.com/matomo.php";

  @Mock
  private AppSettingService appSettingService;

  private MatomoTrackerBuilder builder;

  private final Map<String, String> matomoSettings = Map.ofEntries(
      Map.entry(AppSettingKey.MATOMO_SITE_ID.getKey(), SITE_ID),
      Map.entry(AppSettingKey.MATOMO_API_ENDPOINT.getKey(), "https://matomo.example.com/matomo.php"),
      Map.entry(AppSettingKey.MATOMO_ENABLED.getKey(), "false"));

  @BeforeEach
  void setUp() {
    builder = new MatomoTrackerBuilder(appSettingService) {
      @Override
      protected AppSettingKey getEndpointKey() {
        return AppSettingKey.MATOMO_STABLE_API_ENDPOINT;
      }

      @Override
      protected AppSettingKey getSiteIdKey() {
        return AppSettingKey.MATOMO_STABLE_SITE_ID;
      }

      @Override
      protected AppSettingKey getEnabledKey() {
        return AppSettingKey.MATOMO_STABLE_ENABLED;
      }
    };
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
        matomoSettings(SITE_ID, API_ENDPOINT, "false")).thenReturn(
        matomoSettings("789", API_ENDPOINT, "false"));

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when site ID changes");
  }

  @Test
  void testBuildCreatesNewTrackerWhenEnabledChanges() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(
        matomoSettings(SITE_ID, API_ENDPOINT, "false")).thenReturn(
        matomoSettings(SITE_ID, API_ENDPOINT, "true"));

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when enabled setting changes");
  }

  @Test
  void testBuildReturnsPreviousTrackerWhenEndpointChanges() {
    when(appSettingService.getByCategory(AppSettingCategory.MATOMO)).thenReturn(
        matomoSettings(SITE_ID, API_ENDPOINT, "false")).thenReturn(
        matomoSettings(SITE_ID, "invalid-uri", "false"));

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