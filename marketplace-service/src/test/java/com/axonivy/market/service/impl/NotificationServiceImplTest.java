package com.axonivy.market.service.impl;

import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.SecurityFeature;
import com.axonivy.market.github.model.SecurityMonitorMailProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

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
  void notify_shouldReturnImmediately_whenEventsAreEmpty() {
    notificationService.notify(List.of());
    verifyNoInteractions(mailSender);
  }

  @Test
  void notify_shouldSendEmail_whenEventsExist() {
    when(mailProperties.getFrom()).thenReturn("from@test.com");
    when(mailProperties.getTo()).thenReturn("to@test.com");
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

    ArgumentCaptor<SimpleMailMessage> captor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);

    notificationService.notify(events);

    verify(mailSender).send(captor.capture());

    SimpleMailMessage message = captor.getValue();

    assertEquals("from@test.com", message.getFrom());
    assertNotNull(message.getTo());
    assertEquals("to@test.com", message.getTo()[0]);
    assertEquals(
        "[Security Monitor] Disabled security events detected (2)",
        message.getSubject()
    );

    String body = message.getText();

    assertNotNull(body);
    assertTrue(body.contains("1. basic-workflow-ui"));
    assertTrue(body.contains(
        "https://github.com/axonivy-market/basic-workflow-ui/security"
    ));
    assertTrue(body.contains(SecurityFeature.DEPENDABOT.name()));
    assertTrue(body.contains(SecurityFeature.CODE_SCANNING.name()));
    assertTrue(body.contains("Security Monitor job"));
  }

  @Test
  void notify_shouldCatchMailException_andNotThrow() {
    when(mailProperties.getFrom()).thenReturn("from@test.com");
    when(mailProperties.getTo()).thenReturn("to@test.com");
    DisabledSecurityEvent dependabotEvent =
        new DisabledSecurityEvent(
            "basic-workflow-ui",
            SecurityFeature.DEPENDABOT,
            AccessLevel.DISABLED
        );

    doThrow(new MailException("SMTP failure") {
    })
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    assertDoesNotThrow(() ->
        notificationService.notify(List.of(dependabotEvent))
    );

    verify(mailSender).send(any(SimpleMailMessage.class));
  }


}