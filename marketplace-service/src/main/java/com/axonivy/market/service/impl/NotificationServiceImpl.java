package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.SecurityMonitorMailProperties;
import com.axonivy.market.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
  public static final String GITHUB_MARKET_ORG_URL = "https://github.com/axonivy-market";
  private final JavaMailSender mailSender;
  private final SecurityMonitorMailProperties mailProperties;

  @Override
  public void notify(List<DisabledSecurityEvent> events) {
    if (events.isEmpty()) {
      return;
    }

    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, "UTF-8");
      message.setFrom(mailProperties.getFrom());
      message.setTo(
          Arrays.stream(mailProperties.getTo().split(CommonConstants.COMMA))
              .map(String::trim)
              .toArray(String[]::new)
      );

      message.setSubject(buildMailSubject(events));
      message.setText(buildBodyHtml(events), true);

      mailSender.send(mimeMessage);
    } catch (MessagingException | MailException ex) {
      log.error("Failed to send security monitor email", ex);
    }
  }

  private String buildMailSubject(List<DisabledSecurityEvent> events) {
    return "[Security Monitor] Disabled security checks detected (" + events.size() + ")";
  }

  private String buildBodyHtml(List<DisabledSecurityEvent> events) {
    Map<String, List<DisabledSecurityEvent>> securityEvents =
        events.stream().collect(Collectors.groupingBy(DisabledSecurityEvent::getRepoName));

    StringBuilder sb = new StringBuilder();
    sb.append("<html><body>");
    sb.append("<p>The following repositories have security checks disabled:</p>");

    AtomicInteger counter = new AtomicInteger(1);

    securityEvents.forEach((repo, repoEvents) -> {
      int index = counter.getAndIncrement();

      sb.append("<p>")
          .append(index).append(". ")
          .append("<a href=\"").append(buildRepoUrl(repo)).append("\">")
          .append("<strong>").append(repo).append("</strong>")
          .append("</a>")
          .append("</p>");

      sb.append("<ul style=\"list-style: none; padding-left: 0;\">");
      repoEvents.forEach(e ->
          sb.append("<li>⛔ ").append(e.getFeature().getSecurityLabel()).append("</li>")
      );
      sb.append("</ul>");
    });

    sb.append("<p>This message was generated automatically by the Security Monitor job.</p>");
    sb.append("</body></html>");

    return sb.toString();
  }

  private String buildRepoUrl(String repoName) {
    return String.format("%s/%s/security", GITHUB_MARKET_ORG_URL, repoName);
  }
}
