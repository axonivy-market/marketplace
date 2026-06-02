package com.axonivy.market.controller;


import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.model.AppSettingDto;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AppSettingController {

  private final AppSettingService service;

  @GetMapping
  @Authorized
  public ResponseEntity<List<AppSettingDto>> getSettings(@RequestParam(required = false) String search) {
    return ResponseEntity.ok(service.search(search));
  }

  @PutMapping("/{key}")
  @Authorized
  public ResponseEntity<AppSettingDto> updateSetting(
      @PathVariable String key,
      @RequestBody AppSettingDto request) {

    return ResponseEntity.ok(service.update(key, request.getSettingValue()));
  }
}