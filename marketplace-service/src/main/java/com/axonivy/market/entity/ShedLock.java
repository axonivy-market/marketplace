package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = SHED_LOCK)
public class ShedLock {

  @Id
  @Column(name = NAME, nullable = false)
  private String name;

  @Column(name = LOCK_UNTIL, nullable = false)
  private Instant lockUntil;

  @Column(name = LOCKED_AT, nullable = false)
  private Instant lockedAt;

  @Column(name = LOCKED_BY, nullable = false)
  private String lockedBy;
}
