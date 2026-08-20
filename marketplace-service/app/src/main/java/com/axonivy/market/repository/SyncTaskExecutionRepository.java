package com.axonivy.market.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskStatus;
import com.axonivy.market.enums.SyncTaskType;

public interface SyncTaskExecutionRepository extends JpaRepository<SyncTaskExecution, String> {
  List<SyncTaskExecution> findByTypeAndNodeNumber(SyncTaskType type, Integer nodeNumber);
  List<SyncTaskExecution> findByNodeNumberAndStatusIn(Integer nodeNumber, Collection<SyncTaskStatus> statuses);

}