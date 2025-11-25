package com.axonivy.market.entity;

import com.axonivy.market.enums.SyncJobStatus;
import com.axonivy.market.enums.SyncJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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

  @Enumerated(EnumType.STRING)
  private SyncJobStatus status;

  @Temporal(TemporalType.TIMESTAMP)
  private Date triggeredAt;

  @Temporal(TemporalType.TIMESTAMP)
  private Date completedAt;

  @Column(length = 1024)
  private String message;

  @Column(length = 255)
  private String reference;
}
