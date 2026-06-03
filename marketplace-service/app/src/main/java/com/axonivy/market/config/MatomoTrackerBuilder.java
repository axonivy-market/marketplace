package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.TrackerConfiguration;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Objects;

/**
 * Builds and caches a MatomoTracker instance using runtime values from AppSettingService. * Rebuilds only when the
 * relevant settings change.
 */
@Component
@RequiredArgsConstructor
public class MatomoTrackerBuilder {

  private final AppSettingService appSettingService;
  private volatile MatomoTracker tracker;

  private String endpoint;
  private Integer siteId;
  private Boolean enabled;

  public synchronized MatomoTracker create() {
    String rawEndpoint = appSettingService.getValueByKey(AppSettingKey.MATOMO_API_ENDPOINT).trim();
    String rawSiteId = appSettingService.getValueByKey(AppSettingKey.MATOMO_SITE_ID).trim();
    boolean newEnabled = Boolean.parseBoolean(appSettingService.getValueByKey(AppSettingKey.MATOMO_ENABLED).trim());

    int newSiteId;
    try {
      newSiteId = Integer.parseInt(rawSiteId);
    } catch (NumberFormatException ex) {
      newSiteId = 1;
    }

    if (tracker == null
        || !Objects.equals(endpoint, rawEndpoint)
        || !Objects.equals(siteId, newSiteId)
        || !Objects.equals(enabled, newEnabled)) {
      return tracker;
    }

    try {
      TrackerConfiguration config = TrackerConfiguration.builder()
          .apiEndpoint(URI.create(rawEndpoint))
          .defaultSiteId(newSiteId)
          .enabled(newEnabled)
          .build();

      // update cached values
      tracker = new MatomoTracker(config);
      endpoint = rawEndpoint;
      siteId = newSiteId;
      enabled = newEnabled;

      return tracker;
    } catch (Exception ex) {
      return tracker;
    }
  }
}
