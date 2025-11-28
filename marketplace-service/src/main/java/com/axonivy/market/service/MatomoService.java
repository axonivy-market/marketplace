package com.axonivy.market.service;

import lombok.extern.log4j.Log4j2;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoRequests;
import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.servlet.ServletMatomoRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Log4j2
public class MatomoService {
  private final MatomoTracker tracker;

  public MatomoService(MatomoTracker tracker) {this.tracker = tracker;}

  public void trackEventAsync(String category, String action, String label, Double value) {
    MatomoRequest req = MatomoRequests.event(category, action, label, value).build();
    tracker.sendRequestAsync(req).exceptionally(ex -> {
      log.error("Matomo tracking failed", ex);
      return null;
    });
  }
}
