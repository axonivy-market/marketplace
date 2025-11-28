package com.axonivy.market.config;

import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.TrackerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class MatomoConfig {
  @Bean
  public TrackerConfiguration matomoTrackerConfiguration() {
    return TrackerConfiguration.builder()
        .apiEndpoint(URI.create("http://marketplace.server.ivy-cloud.com:8085/matomo.php"))
        .defaultSiteId(1)
        .build();
  }

  @Bean
  public MatomoTracker matomoTracker(TrackerConfiguration config) {
    return new MatomoTracker(config);
  }
}
