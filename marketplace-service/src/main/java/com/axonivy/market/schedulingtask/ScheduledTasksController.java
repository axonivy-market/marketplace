package com.axonivy.market.schedulingtask;

import com.axonivy.market.model.ScheduledTaskModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.SCHEDULED_TASK;

@RestController
@RequiredArgsConstructor
@RequestMapping(SCHEDULED_TASK)
public class ScheduledTasksController {

  private final ScheduledTaskServiceImpl scheduledTaskService;

  @GetMapping
  public List<ScheduledTaskModel> list() {
    return scheduledTaskService.all().stream()
        .sorted((a, b) -> a.getId().compareToIgnoreCase(b.getId()))
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ScheduledTaskModel> one(@PathVariable String id) {
    return scheduledTaskService.all().stream()
        .filter(info -> info.getId().equals(id))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
