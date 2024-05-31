package com.axonivy.market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AppController {

  @GetMapping
  public ResponseEntity<String> init() {
    // TODO 
    return ResponseEntity.ok("Welcome to Markketplace API");
  }
}
