package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.factory.DisabledSecurityEventFactory;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import com.axonivy.market.service.NotificationService;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;

import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class SchedulingTasksTest {

  @SpyBean
  ScheduledTasks tasks;

  @MockBean
  GitHubService gitHubService;

  @MockBean
  NotificationService notificationService;

  @MockBean
  GitHubProperty gitHubProperty;

  @Test
  void testShouldNotTriggerAfterApplicationStarted() {
    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductFromGitHubRepo());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductDocuments());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductMavenDependencies());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductReleases());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForSecurityMonitor());
  }

  @Test
  void shouldSendNotificationWhenSecurityChecksAreDisabled() throws Exception {
    String token = "dummy-token";
    when(gitHubProperty.getToken()).thenReturn(token);

    ProductSecurityInfo securityInfo = mock(ProductSecurityInfo.class);
    when(gitHubService.getSecurityDetailsForAllProducts(
        eq(token),
        eq(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME)
    )).thenReturn(List.of(securityInfo));

    DisabledSecurityEvent event = mock(DisabledSecurityEvent.class);

    try (MockedStatic<DisabledSecurityEventFactory> mockedFactory =
             Mockito.mockStatic(DisabledSecurityEventFactory.class)) {

      mockedFactory
          .when(() -> DisabledSecurityEventFactory.from(securityInfo))
          .thenReturn(List.of(event));

      tasks.syncDataForSecurityMonitor();
      verify(notificationService).notify(List.of(event));
    }
  }
}
