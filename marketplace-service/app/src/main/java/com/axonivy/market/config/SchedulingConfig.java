package com.axonivy.market.config;

import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.schedulingtask.ScheduledTasks;
import com.axonivy.market.core.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Instant;

@Log4j2
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig implements SchedulingConfigurer {

  private static final String THREAD_NAME_PREFIX = "SC-Thread-";
  private static final int POOL_SIZE = 10;
  private static final int NODE_DIVISOR = 2;

  private final ObjectProvider<AppSettingService> appSettingService;
  private final ObjectProvider<ScheduledTasks> scheduledTasks;

  @Value("${market.node-number:1}")
  private int nodeNumber;

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    // Keep scheduled triggers on a bounded scheduler; cron coordination should not fan out unboundedly.
    var taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(POOL_SIZE);
    taskScheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);

    // Don't wait for running tasks during shutdown
    taskScheduler.setWaitForTasksToCompleteOnShutdown(false);
    taskScheduler.setAwaitTerminationSeconds(0);

    taskScheduler.initialize();
    return taskScheduler;
  }

  @Override
  public void configureTasks(@NotNull ScheduledTaskRegistrar registrar) {
    if (nodeNumber % NODE_DIVISOR == 0) {
      log.info("Skipping scheduled tasks on node {}", nodeNumber);
      return;
    }

    registrar.setTaskScheduler(taskScheduler());

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncDataForProductFromGitHubRepo(),
        context -> nextExecution(AppSettingKey.PRODUCTS_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncDataForProductDocuments(),
        context -> nextExecution(AppSettingKey.DOCUMENTS_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncDataForProductMavenDependencies(),
        context -> nextExecution(AppSettingKey.PRODUCTS_DEPENDENCY_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncDataForProductReleases(),
        context -> nextExecution(AppSettingKey.PRODUCT_RELEASE_NOTES_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncDataForGithubRepos(),
        context -> nextExecution(AppSettingKey.GITHUB_REPOS_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().sendNotificationForSecurityMonitor(),
        context -> nextExecution(AppSettingKey.SEND_NOTIFICATION_SECURITY_MONITOR_CRON, context));

    registrar.addTriggerTask(() -> scheduledTasks.getObject().syncSecurityMonitor(),
        context -> nextExecution(AppSettingKey.SECURITY_MONITOR_CRON, context));
  }

  /**
    * Get the next execution time based on the cron expression from the app settings.
    * If the cron expression is invalid, it will log a warning and use the default value.
  */
  private Instant nextExecution(AppSettingKey key, TriggerContext context) {
    var cron = appSettingService.getObject().getStringValueByKey(key);
    try {
      return new CronTrigger(cron).nextExecution(context);
    } catch (IllegalArgumentException ex) {
      log.warn("Invalid cron expression for key '{}': {}. Using default value: {}",
          key.getKey(), cron, key.getDefaultValue(), ex);
      return new CronTrigger(key.getDefaultValue()).nextExecution(context);
    }
  }
}
