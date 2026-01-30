package com.axonivy.market.service.impl;

import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.enums.SecurityFeature;
import com.axonivy.market.github.model.SecurityMonitorMailProperties;
import jakarta.mail.Address;
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
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
    assertThat(sentMessage.getFrom()[0])
        .as("Sender address should match the configured mail property")
        .hasToString("from@test.com");

    assertThat(sentMessage.getAllRecipients()[0])
        .as("Recipient address should match the configured mail property")
        .hasToString("to@test.com");

    assertThat(sentMessage.getSubject())
        .as("Email subject should contain the number of disabled security checks")
        .isEqualTo("[Security Monitor] Disabled security checks detected (2)");

    String body = sentMessage.getContent().toString();
    assertThat(body)
        .as("Email body should contain repository info and disabled security features")
        .contains("<strong>basic-workflow-ui</strong>")
        .contains("https://github.com/axonivy-market/basic-workflow-ui/security")
        .contains("⛔ Dependabot")
        .contains("⛔ Code Scanning");
  }

  @Test
  void testNotifySupportsMultipleRecipients() throws Exception {
    when(mailProperties.getFrom()).thenReturn("from@test.com");
    when(mailProperties.getTo()).thenReturn("to1@test.com, to2@test.com, to3@test.com");
    MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    DisabledSecurityEvent event =
        new DisabledSecurityEvent(
            "basic-workflow-ui",
            SecurityFeature.DEPENDABOT,
            AccessLevel.DISABLED
        );

    notificationService.notify(List.of(event));

    ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailSender).send(captor.capture());

    MimeMessage sent = captor.getValue();

    Address[] recipients = sent.getAllRecipients();
    assertThat(recipients)
        .as("Email should be sent to all configured recipients")
        .hasSize(3);

    assertThat(recipients[0])
        .as("First recipient address should match configuration")
        .hasToString("to1@test.com");

    assertThat(recipients[1])
        .as("Second recipient address should match configuration")
        .hasToString("to2@test.com");

    assertThat(recipients[2])
        .as("Third recipient address should match configuration")
        .hasToString("to3@test.com");
  }

  @Test
  void testNotifyDoNothingIfAllAreEnabled() {
    notificationService.notify(List.of());
    assertThatCode(() -> verifyNoInteractions(mailSender))
        .as("Mail sender should not be invoked when there are no disabled security events")
        .doesNotThrowAnyException();
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

    assertThatCode(() -> notificationService.notify(List.of(event)))
        .as("Notification service should catch mail exceptions and not propagate them")
        .doesNotThrowAnyException();
    verify(mailSender).send(any(MimeMessage.class));
  }
}