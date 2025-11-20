import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ScheduledTasksService } from './scheduled-tasks.service';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-scheduled-tasks',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './scheduled-tasks.component.html',
  styleUrls: ['./scheduled-tasks.component.scss']
})
export class ScheduledTasksComponent implements OnInit {
  private readonly service = inject(ScheduledTasksService);
  readonly refreshMs = 15000;
  tasks$!: Observable<ScheduledTaskInfo[]>;

  ngOnInit() {
    // polling stream
    this.tasks$ = this.service.poll(this.refreshMs);
  }

  formatDate(value?: string | null): string {
    if (!value) return '';
    return new Date(value).toLocaleString();
  }
}
