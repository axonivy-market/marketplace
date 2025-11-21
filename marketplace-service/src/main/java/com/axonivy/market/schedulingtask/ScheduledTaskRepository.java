package com.axonivy.market.schedulingtask;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTaskEntity, String> {
}