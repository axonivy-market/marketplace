import {
  inject,
  Injectable,
  PLATFORM_ID,
  signal,
  WritableSignal
} from '@angular/core';
import {
  API_INTERNAL_URL,
  API_PUBLIC_URL,
  API_URI
} from '../../../shared/constants/api.constant';
import { environment } from '../../../../environments/environment.development';
import { isPlatformBrowser, isPlatformServer } from '@angular/common';
import { RuntimeConfigService } from '../../configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from '../../models/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class LogStreamService {
  private eventSource?: EventSource;
  private readonly platformId = inject(PLATFORM_ID);
  private readonly runtimeConfig = inject(RuntimeConfigService);
  private readonly apiInternalUrl = inject(API_INTERNAL_URL, {
    optional: true
  });
  private readonly apiPublicUrl = inject(API_PUBLIC_URL, {
    optional: true
  });

  private readonly _logs: WritableSignal<string[]> = signal([]);
  readonly logs = this._logs.asReadonly();

  private readonly MAX_LINES = 2000;

  connect(): void {
    if (this.eventSource || isPlatformServer(this.platformId)) return;

    let baseUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);

    if (isPlatformBrowser(this.platformId)) {
      baseUrl = this.apiPublicUrl || baseUrl;
    } else {
      baseUrl = this.apiInternalUrl || environment.apiInternalUrl;
    }

    const logsUrl = `${baseUrl}/${API_URI.LOGS}/stream`;
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
