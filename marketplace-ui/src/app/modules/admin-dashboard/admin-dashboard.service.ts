import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';
import { API_URI } from '../../shared/constants/api.constant';

@Injectable({ providedIn: 'root' })
export class ScheduledTasksService {
  constructor(private readonly http: HttpClient) {}

  getScheduledTask(): Observable<ScheduledTaskInfo[]> {
    const url = `${API_URI.SCHEDULED_TASK}`;
    return this.http.get<ScheduledTaskInfo[]>(url);
  }
}