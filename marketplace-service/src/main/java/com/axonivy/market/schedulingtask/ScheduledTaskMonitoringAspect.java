package com.axonivy.market.schedulingtask;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class ScheduledTaskMonitoringAspect {

  private final ScheduledTaskRegistry registry;
  private final Environment environment;

  @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
  public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    String cron = scheduled.cron();
    String resolvedCron = resolveCron(cron);
    String id = method.getDeclaringClass().getSimpleName() + "#" + method.getName();

    registry.beforeExecute(id, resolvedCron);
    try {
      Object result = pjp.proceed();
      registry.afterSuccess(id);
      return result;
    } catch (Throwable t) {
      registry.afterFailure(id, t);
      log.warn("Scheduled task {} failed", id, t);
      throw t;
    }
  }

  private String resolveCron(String cron) {
    if (cron == null || cron.isBlank()) {
      return cron;
    }
    // property placeholder like ${market.scheduling.products-cron}
    if (cron.startsWith("${") && cron.endsWith("}")) {
      String key = cron.substring(2, cron.length() - 1);
      String value = environment.getProperty(key);
      return value != null ? value : cron;
    }
    return cron;
  }
}
