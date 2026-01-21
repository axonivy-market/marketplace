import { Injectable, signal, WritableSignal } from '@angular/core';
import { API_URI } from '../../../shared/constants/api.constant';
import { environment } from '../../../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class LogStreamService {
  private eventSource?: EventSource;

  private readonly _logs: WritableSignal<string[]> = signal([]);
  readonly logs = this._logs.asReadonly();

  private readonly MAX_LINES = 2000;

  connect(): void {
    if (this.eventSource) return;

    const logsUrl = `${environment.apiInternalUrl}/${API_URI.LOGS}/stream`;
    this.eventSource = new EventSource(logsUrl);

    this.eventSource.onmessage = event => {
      this._logs.update(lines => {
        const next = [...lines, event.data];
        return next.length > this.MAX_LINES
          ? next.slice(next.length - this.MAX_LINES)
          : next;
      });
    };

    this.eventSource.onerror = (error: Event) => {
      console.error('EventSource connection error:', {
        readyState: this.eventSource?.readyState,
        url: this.eventSource?.url,
        error
      });
      this.disconnect();
    };
  }

  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = undefined;
  }

  clear(): void {
    this._logs.set([]);
  }

  isConnected(): boolean {
    return !!this.eventSource;
  }
}
