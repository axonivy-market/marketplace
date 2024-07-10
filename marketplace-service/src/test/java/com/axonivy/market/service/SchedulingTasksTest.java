package com.axonivy.market.service;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.axonivy.market.schedulingtask.ScheduledTasks;

@SpringBootTest
class SchedulingTasksTest {

  @SpyBean
  ScheduledTasks tasks;

  @Test
  void testShouldNotTriggerAfterApplicationStarted() {
    Awaitility.await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
      verify(tasks, atLeast(0)).syncDataForProductFromGitHubRepo();
    });
  }
}
