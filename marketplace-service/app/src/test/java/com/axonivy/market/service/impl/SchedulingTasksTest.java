package com.axonivy.market.service.impl;

import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.factory.DisabledSecurityEventFactory;
import com.axonivy.market.github.model.DisabledSecurityEvent;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.NotificationService;
import com.axonivy.market.service.ProductDependencyService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.controller.ProductDetailsController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingTasksTest {

  @InjectMocks
  ScheduledTasks tasks;

  @Mock
  private com.axonivy.market.repository.ProductRepository productRepo;

  @Mock
  private ProductService productService;

  @Mock
  private ProductDetailsController productDetailsController;

  @Mock
  private ExternalDocumentService externalDocumentService;

  @Mock
  private ProductDependencyService productDependencyService;

  @Mock
  private GithubReposService gitHubReposService;

  @Mock
  GitHubService gitHubService;

  @Mock
  ProductSecurityInfoRepository productSecurityInfoRepository;

  @Mock
  NotificationService notificationService;

  @Test
  void testShouldNotTriggerAfterApplicationStarted() {
    verifyNoInteractions(productService, productRepo, productDetailsController, externalDocumentService,
        productDependencyService, gitHubReposService, gitHubService, productSecurityInfoRepository,
        notificationService);
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
    DisabledSecurityEvent event = mock(DisabledSecurityEvent.class);
    when(productSecurityInfoRepository.findAll()).thenReturn(List.of(securityInfo));
    try (MockedStatic<DisabledSecurityEventFactory> mockedFactory =
             Mockito.mockStatic(DisabledSecurityEventFactory.class)) {
      mockedFactory.when(() -> DisabledSecurityEventFactory.from(securityInfo)).thenReturn(List.of(event));
      tasks.sendNotificationForSecurityMonitor();
      verify(notificationService).notify(List.of(event));
    }
  }
}
