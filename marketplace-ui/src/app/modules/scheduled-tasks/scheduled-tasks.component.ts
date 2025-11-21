import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { ScheduledTasksService } from './scheduled-tasks.service';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-scheduled-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './scheduled-tasks.component.html',
  styleUrls: ['./scheduled-tasks.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class ScheduledTasksComponent implements OnInit {
  private readonly service = inject(ScheduledTasksService);
  readonly refreshMs = 15000;
  tasks$!: Observable<ScheduledTaskInfo[]>;

  ngOnInit() {
    this.tasks$ = this.service.pollWithImmediate(this.refreshMs);
  }

  formatDate(value?: string | null): string {
    if (!value) return '';
    const date = new Date(value);
    return date.toLocaleString();
  }
}
