package com.axonivy.market.service;

import com.axonivy.market.github.model.DisabledSecurityEvent;

import java.util.List;

public interface NotificationService {

  /**
   * <p>
   * Sends notification emails to administrators about disabled or compromised security events in monitored
   * repositories. Events include security scanning failures, dependency vulnerabilities, or code scanning alerts.
   * Composes MIME email messages with event details and sends them via configured SMTP server.
   * </p>
   *
   * @param  events
   *              type {@link List<DisabledSecurityEvent>} - list of security events to notify about, each containing
   *              repository name, event type, severity, and recommended actions
   * @return void - no return value; email notifications are sent asynchronously
   * @author nntthuy
   */
  void notify(List<DisabledSecurityEvent> events);
}