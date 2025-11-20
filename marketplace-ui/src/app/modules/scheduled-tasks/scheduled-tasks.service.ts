import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';

@Injectable({ providedIn: 'root' })
export class ScheduledTasksService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl + '/api/scheduled-tasks';

  list(): Observable<ScheduledTaskInfo[]> {
    return this.http.get<ScheduledTaskInfo[]>(this.baseUrl);
  }

  poll(ms: number): Observable<ScheduledTaskInfo[]> {
    return interval(ms).pipe(switchMap(() => this.list()));
  }
}
