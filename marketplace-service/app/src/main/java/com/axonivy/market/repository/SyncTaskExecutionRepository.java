package com.axonivy.market.repository;

import com.axonivy.market.entity.SyncTaskExecution;
import com.axonivy.market.enums.SyncTaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyncTaskExecutionRepository extends JpaRepository<SyncTaskExecution, String> {
  List<SyncTaskExecution> findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(SyncTaskType type);

  default Optional<SyncTaskExecution> findByType(SyncTaskType type) {
    return findAllByTypeOrderByUpdatedAtDescCreatedAtDesc(type).stream()
        .findFirst();
  }

}