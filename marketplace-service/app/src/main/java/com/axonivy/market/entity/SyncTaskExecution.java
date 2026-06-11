package com.axonivy.market.entity;

import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

import static com.axonivy.market.constants.EntityConstants.SYNC_TASK;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = SYNC_TASK)
public class SyncTaskExecution extends AuditableIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;

  @Column(unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private SyncTaskType type;

  @Enumerated(EnumType.STRING)
  private SyncTaskStatus status;

  /**
   * @deprecated
   */
  @Deprecated(forRemoval = true, since = "1.26.0")
  private transient LocalDateTime triggeredAt;

  /**
   * @deprecated
   */
  @Deprecated(forRemoval = true, since = "1.26.0")
  private transient LocalDateTime completedAt;

  private LocalDateTime lastRunDate;

  private LocalDateTime completedDate;

  private String message;

  @Version
  private Integer version;
}
