package com.axonivy.market.schedulingtask;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-tasks")
public class ScheduledTaskController {

  private final ScheduledTaskRegistry registry;

  public ScheduledTaskController(ScheduledTaskRegistry registry) {
    this.registry = registry;
  }

  @GetMapping
  public List<ScheduledTaskInfo> list() {
    return registry.all().stream()
        .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ScheduledTaskInfo> one(@PathVariable String id) {
    return registry.all().stream()
        .filter(info -> info.getId().equals(id))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
