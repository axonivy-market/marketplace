package com.axonivy.market.logging;

import com.axonivy.market.enums.SyncTaskType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TrackSyncTaskExecution {
  SyncTaskType value();
}
