package com.axonivy.market.service.impl;

import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.enums.SecurityFeature;
import com.axonivy.market.github.model.SecurityMonitorMailProperties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private SecurityMonitorMailProperties mailProperties;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Test
  void testNotifySendsEmailWhenDisabledChecksExist() throws Exception {
    when(mailProperties.getFrom()).thenReturn("from@test.com");
    when(mailProperties.getTo()).thenReturn("to@test.com");
    MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    DisabledSecurityEvent dependabotEvent =
        new DisabledSecurityEvent(
            "basic-workflow-ui",
            SecurityFeature.DEPENDABOT,
            AccessLevel.DISABLED
        );

    DisabledSecurityEvent codeScanningEvent =
        new DisabledSecurityEvent(
            "basic-workflow-ui",
            SecurityFeature.CODE_SCANNING,
            AccessLevel.DISABLED
        );

    List<DisabledSecurityEvent> events = List.of(dependabotEvent, codeScanningEvent);

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    notificationService.notify(events);
    verify(mailSender).send(captor.capture());

    MimeMessage sentMessage = captor.getValue();
    assertEquals("from@test.com", sentMessage.getFrom()[0].toString());
    assertEquals("to@test.com", sentMessage.getAllRecipients()[0].toString());
    assertEquals("[Security Monitor] Disabled security checks detected (2)", sentMessage.getSubject());

    String body = sentMessage.getContent().toString();
    assertTrue(body.contains("<strong>basic-workflow-ui</strong>"));
    assertTrue(body.contains("https://github.com/axonivy-market/basic-workflow-ui/security"));
    assertTrue(body.contains("⛔ Dependabot"));
    assertTrue(body.contains("⛔ Code Scanning"));
    assertTrue(body.contains("Security Monitor job"));
  }

  @Test
  void testNotifyDoNothingIfAllAreEnabled() {
    notificationService.notify(List.of());
    verifyNoInteractions(mailSender);
  }

  @Test
  void testNotifyCatchMailError() {
    when(mailProperties.getFrom()).thenReturn("from@test.com");
    when(mailProperties.getTo()).thenReturn("to@test.com");
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));

    doThrow(new MailException("SMTP failure") {})
        .when(mailSender)
        .send(any(MimeMessage.class));

    DisabledSecurityEvent event =
        new DisabledSecurityEvent(
            "basic-workflow-ui",
            SecurityFeature.DEPENDABOT,
            AccessLevel.DISABLED
        );

    assertDoesNotThrow(() -> notificationService.notify(List.of(event)));
    verify(mailSender).send(any(MimeMessage.class));
  }
}