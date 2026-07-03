package com.axonivy.market.repository;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SyncTaskExecutionRepository extends JpaRepository<SyncTaskExecution, String> {
  Optional<SyncTaskExecution> findByType(SyncTaskType type);
  Optional<SyncTaskExecution> findByTypeAndNodeNumber(SyncTaskType type, Integer nodeNumber);
  List<SyncTaskExecution> findByStatusIn(Collection<SyncTaskStatus> statuses);

}