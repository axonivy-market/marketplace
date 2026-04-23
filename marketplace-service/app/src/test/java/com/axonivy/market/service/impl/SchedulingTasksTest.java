package com.axonivy.market.service.impl;

import com.axonivy.market.factory.DisabledSecurityEventFactory;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.NotificationService;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class SchedulingTasksTest {

  @SpyBean
  ScheduledTasks tasks;

  @MockBean
  GitHubService gitHubService;

  @MockBean
  GithubReposService gitHubReposService;

  @MockBean
  ProductSecurityInfoRepository productSecurityInfoRepository;

  @MockBean
  NotificationService notificationService;

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
        .untilAsserted(() -> verify(tasks, atLeast(0)).sendNotificationForSecurityMonitor());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncSecurityMonitor());
  }

  @Test
  void testShouldHandleIOExceptionWhenSyncingGithubRepos() throws Exception {
    doThrow(new IOException("failure")).when(gitHubReposService).loadAndStoreTestReports();
    assertDoesNotThrow(() -> tasks.syncDataForGithubRepos(),
        "syncDataForGithubRepos should swallow IOException and not propagate it");
    verify(gitHubReposService).loadAndStoreTestReports();
  }

  @Test
  void testSyncSecurityMonitorShouldCallSyncSecurityDetailsForProduct() throws Exception {
    when(gitHubService.syncSecurityDetailsForProduct()).thenReturn(List.of());
    assertDoesNotThrow(() -> tasks.syncSecurityMonitor(),
        "syncSecurityMonitor should not propagate any exception");
    verify(gitHubService).syncSecurityDetailsForProduct();
  }

  @Test
  void testSyncSecurityMonitorShouldSwallowIOException() throws Exception {
    doThrow(new IOException("GitHub API unavailable")).when(gitHubService).syncSecurityDetailsForProduct();
    assertDoesNotThrow(() -> tasks.syncSecurityMonitor(),
        "syncSecurityMonitor should swallow IOException and not propagate it");
    verify(gitHubService).syncSecurityDetailsForProduct();
  }

  @Test
  void testShouldSendNotificationWhenSecurityChecksAreDisabled() throws Exception {
    ProductSecurityInfo securityInfo = mock(ProductSecurityInfo.class);
    when(gitHubService.syncSecurityDetailsForProduct()).thenReturn(List.of(securityInfo));
    DisabledSecurityEvent event = mock(DisabledSecurityEvent.class);
    // Mock repository to return the same ProductSecurityInfo as in the service
    when(productSecurityInfoRepository.findAll()).thenReturn(List.of(securityInfo));
    try (MockedStatic<DisabledSecurityEventFactory> mockedFactory =
             Mockito.mockStatic(DisabledSecurityEventFactory.class)) {
      mockedFactory.when(() -> DisabledSecurityEventFactory.from(securityInfo)).thenReturn(List.of(event));
      tasks.sendNotificationForSecurityMonitor();
      verify(notificationService).notify(List.of(event));
    }
  }
}
