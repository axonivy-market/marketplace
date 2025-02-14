package com.axonivy.market.service.impl;

import com.axonivy.market.schedulingtask.ScheduledTasks;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"MONGODB_USERNAME=user", "MONGODB_PASSWORD=password", "MONGODB_HOST=mongoHost",
    "MONGODB_DATABASE=product", "MARKET_GITHUB_OAUTH_APP_CLIENT_ID=clientId",
    "MARKET_GITHUB_OAUTH_APP_CLIENT_SECRET=clientSecret", "MARKET_JWT_SECRET_KEY=jwtSecret",
    "MARKET_CORS_ALLOWED_ORIGIN=*", "MARKET_GITHUB_MARKET_BRANCH=master", "MARKET_MONGO_LOG_LEVEL=DEBUG",
    "MARKET_LOG_PATH=logs", "MARKET_GITHUB_TOKEN=test", "MARKET_CLICK_LIMIT=5",
    "MARKET_LIMITED_REQUEST_PATHS=/api/test"})
class SchedulingTasksTest {

  @SpyBean
  ScheduledTasks tasks;

  @Test
  void testShouldNotTriggerAfterApplicationStarted() {
    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductFromGitHubRepo());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductDocuments());

    Awaitility.await().atMost(Durations.TEN_SECONDS)
        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductMavenDependencies());
  }

}
