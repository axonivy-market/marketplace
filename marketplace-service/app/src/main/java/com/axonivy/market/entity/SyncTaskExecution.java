package com.axonivy.market.entity;

import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

import static com.axonivy.market.constants.EntityConstants.SYNC_TASK_EXECUTION;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = SYNC_TASK_EXECUTION)
public class SyncTaskExecution extends AuditableIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  private SyncTaskType type;

  @Enumerated(EnumType.STRING)
  private SyncTaskStatus status;

  @Deprecated(forRemoval = true, since = "1.26.0")
  private LocalDateTime triggeredAt;

  @Deprecated(forRemoval = true, since = "1.26.0")
  private LocalDateTime completedAt;

  private LocalDateTime lastRunDate;

  private LocalDateTime completedDate;

  private String message;
}
