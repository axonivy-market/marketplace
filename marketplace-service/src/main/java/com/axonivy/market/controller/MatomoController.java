package com.axonivy.market.controller;

import com.axonivy.market.service.MatomoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatomoController {
  private final MatomoService matomoService;

  public MatomoController(MatomoService matomoService) {this.matomoService = matomoService;}

  @GetMapping("/buy")
  public ResponseEntity<String> buy(HttpServletRequest httpReq) {
    matomoService.trackEventAsync(httpReq);
    return ResponseEntity.ok("order placed");
  }
}
