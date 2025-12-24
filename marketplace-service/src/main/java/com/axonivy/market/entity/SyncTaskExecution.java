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
import java.time.LocalDate;

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

  private LocalDate triggeredAt;

  private LocalDate completedAt;

  private String message;
}
