package com.axonivy.market.service;

import com.axonivy.market.github.model.DisabledSecurityEvent;

import java.util.List;

public interface NotificationService {
  void notify(List<DisabledSecurityEvent> events);
}