package com.axonivy.market.schedulingtask;

import com.axonivy.market.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, String> {
}