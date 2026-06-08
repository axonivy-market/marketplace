package com.axonivy.market.config;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.Instant;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig implements SchedulingConfigurer {

  private static final String THREAD_NAME_PREFIX = "SC-Thread-";
  private static final int POOL_SIZE = 10;
  private static final Duration NODE_2_OFFSET = Duration.ofMinutes(15);
  private static final int NODE_2 = 2;

  private final AppSettingService appSettingService;
  private final ScheduledTasks scheduledTasks;

  @Value("${market.node-number:1}")
  private int nodeNumber;

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    var taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(POOL_SIZE);
    taskScheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
    taskScheduler.initialize();
    return taskScheduler;
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {

    registrar.setTaskScheduler(taskScheduler());

    registrar.addTriggerTask(scheduledTasks::syncDataForProductFromGitHubRepo,
        context -> nextExecution(AppSettingKey.PRODUCTS_CRON, context));

    registrar.addTriggerTask(scheduledTasks::syncDataForProductDocuments,
        context -> nextExecution(AppSettingKey.DOCUMENTS_CRON, context));

    registrar.addTriggerTask(scheduledTasks::syncDataForProductMavenDependencies,
        context -> nextExecution(AppSettingKey.PRODUCTS_DEPENDENCY_CRON, context));

    registrar.addTriggerTask(scheduledTasks::syncDataForProductReleases,
        context -> nextExecution(AppSettingKey.PRODUCT_RELEASE_NOTES_CRON, context));

    registrar.addTriggerTask(scheduledTasks::syncDataForGithubRepos,
        context -> nextExecution(AppSettingKey.GITHUB_REPOS_CRON, context));

    registrar.addTriggerTask(scheduledTasks::sendNotificationForSecurityMonitor,
        context -> nextExecution(AppSettingKey.SEND_NOTIFICATION_SECURITY_MONITOR_CRON, context));

    registrar.addTriggerTask(scheduledTasks::syncSecurityMonitor,
        context -> nextExecution(AppSettingKey.SECURITY_MONITOR_CRON, context));
  }

  /**
   * Calculates the next execution time based on the cron expression from AppSettingService and applies an offset for
   * even-numbered nodes to reduce concurrent load in clustered deployments.
   */
  private Instant nextExecution(AppSettingKey key, TriggerContext context) {
    Instant next = new CronTrigger(appSettingService.getStringValueByKey(key)).nextExecution(context);
    if (next == null) {
      return null;
    }
    return next.plus(getOffset());
  }

  /**
   * Delay execution on even-numbered nodes to reduce
   * concurrent load in clustered deployments.
   */
  private Duration getOffset() {
    return nodeNumber == NODE_2 ? NODE_2_OFFSET : Duration.ZERO;
  }
}
