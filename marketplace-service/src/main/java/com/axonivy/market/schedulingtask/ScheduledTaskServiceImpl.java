package com.axonivy.market.schedulingtask;

import com.axonivy.market.entity.ScheduledTask;
import com.axonivy.market.enums.JobStatus;
import com.axonivy.market.model.ScheduledTaskModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskServiceImpl {

  private final Map<String, ScheduledTaskModel> tasks = new ConcurrentHashMap<>();
  private final ScheduledTaskRepository repository;

//  public ScheduledTaskServiceImpl(ScheduledTaskRepository repository) {
//    this.repository = repository;
//    // Preload any existing persisted entries
//    repository.findAll().forEach(this::putEntityIntoCache);
//  }

  @Transactional
  public void beforeExecute(String name, String cronExpression) {
    ScheduledTaskModel scheduledTask = tasks.computeIfAbsent(name, k -> {
      ScheduledTaskModel task = new ScheduledTaskModel();
      task.setName(name);
      return task;
    });
    scheduledTask.setCronExpression(cronExpression);
    scheduledTask.setLastStart(new Date());
    scheduledTask.setRunning(true);
    scheduledTask.setStatus(JobStatus.RUNNING);
    save(scheduledTask);
  }

  @Transactional
  public void afterSuccess(String id) {
    ScheduledTaskModel info = tasks.get(id);
    if (info == null) {
      return; // should not happen
    }
//    Instant end = Instant.now();
    info.setLastEnd(new Date());
//    info.setLastSuccessEnd(end);
    info.setRunning(false);
    info.setStatus(JobStatus.SUCCESS);
//    info.setLastSuccess(true);
//    info.setLastError(null);
    computeNext(info);
    save(info);
  }

  @Transactional
  public void afterFailure(String id, Throwable t) {
    ScheduledTaskModel info = tasks.get(id);
    if (info == null) {
      return; // should not happen
    }
//    Instant end = Instant.now();
    info.setLastEnd(new Date());
    info.setRunning(false);
    info.setStatus(JobStatus.FAILED);
//    info.setLastSuccess(false);
//    info.setLastError(t.getClass().getSimpleName() + ": " + t.getMessage());
    computeNext(info); // still compute next cron fire time
    save(info);
  }

  private void computeNext(ScheduledTaskModel info) {
    String cron = info.getCronExpression();
    if (cron == null || cron.isBlank()) {
      info.setNextExecution(null);
      return;
    }
    try {
      CronExpression expression = CronExpression.parse(cron);
      ZonedDateTime next = expression.next(ZonedDateTime.now());
      info.setNextExecution(next == null ? null : Date.from(next.toInstant()));
    } catch (Exception e) {
      // invalid cron – leave nextExecution null
      info.setNextExecution(null);
    }
  }

  public Collection<ScheduledTaskModel> all() {
    return tasks.values();
  }

  private void save(ScheduledTaskModel info) {
    ScheduledTask entity = new ScheduledTask();
    entity.setId(info.getId());
    entity.setCronExpression(info.getCronExpression());
    entity.setLastStart(info.getLastStart());
    entity.setLastEnd(info.getLastEnd());
    entity.setStatus(info.getStatus());
//    entity.setLastSuccessEnd(info.getLastSuccessEnd());
    entity.setNextExecution(info.getNextExecution());
    entity.setRunning(info.isRunning());
//    entity.setLastSuccess(info.isLastSuccess());
//    entity.setLastError(info.getLastError());
    repository.save(entity);
  }

//  private void putEntityIntoCache(ScheduledTask entity) {
//    ScheduledTaskModel info = new ScheduledTaskModel();
//    info.setId(entity.getId());
//    info.setCronExpression(entity.getCronExpression());
//    info.setLastStart(entity.getLastStart());
//    info.setLastEnd(entity.getLastEnd());
//    info.setLastSuccessEnd(entity.getLastSuccessEnd());
//    info.setNextExecution(entity.getNextExecution());
//    info.setRunning(entity.isRunning());
//    info.setLastSuccess(entity.isLastSuccess());
////    info.setLastError(entity.getLastError());
//    tasks.put(info.getId(), info);
//  }
}
