package com.axonivy.market.controller;

import com.axonivy.market.service.MatomoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatomoController {
  private final MatomoService analytics;

  public MatomoController(MatomoService analytics) {this.analytics = analytics;}

  @GetMapping("/buy")
  public ResponseEntity<String> buy() {
    analytics.trackEventAsync("Store","Buy","Product-123", 49.99);
//    analytics.test();
    return ResponseEntity.ok("order placed");
  }

}
