package com.axonivy.market.schedulingtask;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/scheduled-tasks")
@RequiredArgsConstructor
public class ScheduledTasksController {

  private final ScheduledTaskRegistry registry;

  @GetMapping
  public Collection<ScheduledTaskInfo> list() {
    return registry.all();
  }
}
