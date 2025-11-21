package com.axonivy.market.schedulingtask;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduledTaskRegistry {

  private final Map<String, ScheduledTaskInfo> tasks = new ConcurrentHashMap<>();
  private final ScheduledTaskRepository repository;

  public ScheduledTaskRegistry(ScheduledTaskRepository repository) {
    this.repository = repository;
    // Preload any existing persisted entries
    repository.findAll().forEach(this::putEntityIntoCache);
  }

  @Transactional
  public void beforeExecute(String id, String cronExpression) {
    ScheduledTaskInfo info = tasks.computeIfAbsent(id, k -> {
      ScheduledTaskInfo i = new ScheduledTaskInfo();
      i.setId(id);
      return i;
    });
    info.setCronExpression(cronExpression);
    info.setLastStart(Instant.now());
    info.setRunning(true);
    save(info);
  }

  @Transactional
  public void afterSuccess(String id) {
    ScheduledTaskInfo info = tasks.get(id);
    if (info == null) {
      return; // should not happen
    }
    Instant end = Instant.now();
    info.setLastEnd(end);
    info.setLastSuccessEnd(end);
    info.setRunning(false);
    info.setLastSuccess(true);
//    info.setLastError(null);
    computeNext(info);
    save(info);
  }

  @Transactional
  public void afterFailure(String id, Throwable t) {
    ScheduledTaskInfo info = tasks.get(id);
    if (info == null) {
      return; // should not happen
    }
    Instant end = Instant.now();
    info.setLastEnd(end);
    info.setRunning(false);
    info.setLastSuccess(false);
//    info.setLastError(t.getClass().getSimpleName() + ": " + t.getMessage());
    computeNext(info); // still compute next cron fire time
    save(info);
  }

  private void computeNext(ScheduledTaskInfo info) {
    String cron = info.getCronExpression();
    if (cron == null || cron.isBlank()) {
      info.setNextExecution(null);
      return;
    }
    try {
      CronExpression expression = CronExpression.parse(cron);
      ZonedDateTime next = expression.next(ZonedDateTime.now());
      info.setNextExecution(next == null ? null : next.toInstant());
    } catch (Exception e) {
      // invalid cron – leave nextExecution null
      info.setNextExecution(null);
    }
  }

  public Collection<ScheduledTaskInfo> all() {
    return tasks.values();
  }

  private void save(ScheduledTaskInfo info) {
    ScheduledTaskEntity entity = new ScheduledTaskEntity();
    entity.setId(info.getId());
    entity.setCronExpression(info.getCronExpression());
    entity.setLastStart(info.getLastStart());
    entity.setLastEnd(info.getLastEnd());
    entity.setLastSuccessEnd(info.getLastSuccessEnd());
    entity.setNextExecution(info.getNextExecution());
    entity.setRunning(info.isRunning());
    entity.setLastSuccess(info.isLastSuccess());
//    entity.setLastError(info.getLastError());
    repository.save(entity);
  }

  private void putEntityIntoCache(ScheduledTaskEntity entity) {
    ScheduledTaskInfo info = new ScheduledTaskInfo();
    info.setId(entity.getId());
    info.setCronExpression(entity.getCronExpression());
    info.setLastStart(entity.getLastStart());
    info.setLastEnd(entity.getLastEnd());
    info.setLastSuccessEnd(entity.getLastSuccessEnd());
    info.setNextExecution(entity.getNextExecution());
    info.setRunning(entity.isRunning());
    info.setLastSuccess(entity.isLastSuccess());
//    info.setLastError(entity.getLastError());
    tasks.put(info.getId(), info);
  }
}
