package com.axonivy.market.service.impl;

import com.axonivy.market.core.constants.CoreCommonConstants;
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
import java.util.stream.Collectors;

import static com.axonivy.market.constants.MailConstants.*;

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
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, "UTF-8");
      message.setFrom(mailProperties.getFrom());
      message.setTo(
          Arrays.stream(mailProperties.getTo().split(CoreCommonConstants.COMMA))
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

  private String buildBodyHtml(List<DisabledSecurityEvent> disabledSecurityEvents) {
    Map<String, List<DisabledSecurityEvent>> securityEvents =
        disabledSecurityEvents.stream().collect(Collectors.groupingBy(DisabledSecurityEvent::getRepoName));

    StringBuilder sb = new StringBuilder();
    sb.append("<html><body><p>The following repositories have security checks disabled:</p>");

    int index = 1;
    for (var events : securityEvents.entrySet()) {
      sb.append(REPO_NAME_HEADER_FORMAT.formatted(index, buildRepoUrl(events.getKey()), events.getKey()));
      index++;
      sb.append(UL_START);
      events.getValue().forEach(e -> sb.append(LI_FORMAT.formatted(e.getFeature().getSecurityLabel())));
      sb.append(UL_END);
    }

    sb.append("<p>This message was generated automatically by the Security Monitor job.</p></body></html>");
    return sb.toString();
  }

  private String buildRepoUrl(String repoName) {
    return String.format("%s/%s/security", GITHUB_MARKET_ORG_URL, repoName);
  }
}
