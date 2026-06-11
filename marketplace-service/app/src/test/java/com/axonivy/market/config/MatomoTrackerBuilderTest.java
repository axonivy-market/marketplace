package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.matomo.java.tracking.MatomoTracker;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatomoTrackerBuilderTest {

  @Mock
  private AppSettingService appSettingService;

  private MatomoTrackerBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new MatomoTrackerBuilder(appSettingService);
  }

  private void stubMatomoSettings(String endpoint, String siteId, boolean enabled) {
    when(appSettingService.getStringValueByKey(AppSettingKey.MATOMO_API_ENDPOINT)).thenReturn(endpoint);
    when(appSettingService.getStringValueByKey(AppSettingKey.MATOMO_SITE_ID)).thenReturn(siteId);
    when(appSettingService.getBooleanValueByKey(AppSettingKey.MATOMO_ENABLED)).thenReturn(enabled);
  }

  @Test
  void testBuildCreatesMatomoTracker() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", true);

    MatomoTracker tracker = builder.build();

    assertNotNull(tracker, "MatomoTracker should not be null");
  }

  @Test
  void testBuildReturnsCachedTrackerWhenSettingsUnchanged() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", true);

    MatomoTracker first = builder.build();
    MatomoTracker second = builder.build();

    assertSame(first, second, "Should return cached tracker when settings have not changed");
  }

  @Test
  void testBuildCreatesNewTrackerWhenEndpointChanges() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", true);
    MatomoTracker first = builder.build();

    stubMatomoSettings("https://new-matomo.example.com/matomo.php", "1", true);
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when endpoint changes");
  }

  @Test
  void testBuildCreatesNewTrackerWhenSiteIdChanges() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", true);
    MatomoTracker first = builder.build();

    stubMatomoSettings("https://matomo.example.com/matomo.php", "2", true);
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when site ID changes");
  }

  @Test
  void testBuildCreatesNewTrackerWhenEnabledChanges() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", true);
    MatomoTracker first = builder.build();

    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", false);
    MatomoTracker second = builder.build();

    assertNotSame(first, second, "Should create new tracker when enabled flag changes");
  }

  @Test
  void testBuildDisabledTracker() {
    stubMatomoSettings("https://matomo.example.com/matomo.php", "1", false);

    MatomoTracker tracker = builder.build();

    assertNotNull(tracker, "Should create tracker even when disabled");
  }
}

