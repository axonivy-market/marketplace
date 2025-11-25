package com.axonivy.market.repository;

import com.axonivy.market.entity.SyncJobExecution;
import com.axonivy.market.enums.SyncJobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncJobExecutionRepository extends JpaRepository<SyncJobExecution, String> {
	Optional<SyncJobExecution> findTopByJobTypeOrderByTriggeredAtDesc(SyncJobType jobType);
}
