package com.axonivy.market.service.impl;

import com.axonivy.market.schedulingtask.ScheduledTasks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest(properties = {"MONGODB_USERNAME=user", "MONGODB_PASSWORD=password", "MONGODB_HOST=mongoHost",
    "MONGODB_DATABASE=product", "MARKET_GITHUB_OAUTH_APP_CLIENT_ID=clientId",
    "MARKET_GITHUB_OAUTH_APP_CLIENT_SECRET=clientSecret", "MARKET_JWT_SECRET_KEY=jwtSecret",
    "MARKET_CORS_ALLOWED_ORIGIN=*", "MARKET_GITHUB_MARKET_BRANCH=master", "MARKET_MONGO_LOG_LEVEL=DEBUG"})
class SchedulingTasksTest {

  @SpyBean
  ScheduledTasks tasks;

  //TODO
//  @Test
//  void testShouldNotTriggerAfterApplicationStarted() {
//    Awaitility.await().atMost(Durations.TEN_SECONDS)
//        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductFromGitHubRepo());
//
//    Awaitility.await().atMost(Durations.TEN_SECONDS)
//        .untilAsserted(() -> verify(tasks, atLeast(0)).syncDataForProductDocuments());
//  }

}
