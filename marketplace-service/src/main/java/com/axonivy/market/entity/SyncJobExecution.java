package com.axonivy.market.entity;

import com.axonivy.market.enums.SyncJobStatus;
import com.axonivy.market.enums.SyncJobType;
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
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.SYNC_JOB_EXECUTION;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = SYNC_JOB_EXECUTION)
public class SyncJobExecution extends AuditableIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;

  @Enumerated(EnumType.STRING)
  private SyncJobType jobType;

  private String jobKey;

  @Enumerated(EnumType.STRING)
  private SyncJobStatus status;

  private Date triggeredAt;

  private Date completedAt;

  private String message;
}
