package com.axonivy.market.service.impl;

import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.SecurityMonitorMailProperties;
import com.axonivy.market.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  private final JavaMailSender mailSender;
  private final SecurityMonitorMailProperties mailProperties;

  @Override
  public void notify(List<DisabledSecurityEvent> events) {
    if (events.isEmpty()) {
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(mailProperties.getFrom());
      message.setTo(mailProperties.getTo());
      message.setSubject(buildSubject(events));
      message.setText(buildBody(events));

      mailSender.send(message);
    } catch (MailException ex) {
      log.error("Failed to send security monitor email", ex);
    }
  }

  private String buildSubject(List<DisabledSecurityEvent> events) {
    return "[Security Monitor] Disabled security events detected (" + events.size() + ")";
  }

  private String buildBody(List<DisabledSecurityEvent> events) {
    Map<String, List<DisabledSecurityEvent>> securityEvents =
        events.stream().collect(Collectors.groupingBy(DisabledSecurityEvent::repoName));

    StringBuilder sb = new StringBuilder();
    sb.append("The following repositories have disabled security events:\n\n");

    securityEvents.forEach((repo, securityEvent) -> {
      sb.append("Repository: ").append(repo).append("\n");
      securityEvent.forEach(e ->
          sb.append("  - ")
              .append(e.feature()));
      sb.append("\n");
    });

    sb.append("This message was generated automatically by the Security Monitor job.");
    return sb.toString();
  }

}
