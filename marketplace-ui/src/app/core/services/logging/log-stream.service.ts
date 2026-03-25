import {
  inject,
  Injectable,
  PLATFORM_ID,
  signal,
  WritableSignal,
  computed
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
import { HttpParams } from '@angular/common/http';
import { RequestParam } from '../../../shared/enums/request-param';
import {
  EventSourceMessage,
  fetchEventSource
} from '@microsoft/fetch-event-source';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';

@Injectable({
  providedIn: 'root'
})
export class LogStreamService {
  private ctrl: AbortController | null = null;
  private readonly platformId = inject(PLATFORM_ID);
  private readonly runtimeConfig = inject(RuntimeConfigService);
  private readonly adminAuth = inject(AdminAuthService);
  private readonly taskLogs = signal<Map<string, string[]>>(new Map());
  private readonly controllers = new Map<string, AbortController>();
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
    if (this.ctrl || isPlatformServer(this.platformId)) {
      return;
    }

    let baseUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);

    if (isPlatformBrowser(this.platformId)) {
      baseUrl = this.apiPublicUrl || baseUrl;
    } else {
      baseUrl = this.apiInternalUrl || environment.apiInternalUrl;
    }
    let params = new HttpParams();
    const ts = Date.now().toString();
    params = params.set(RequestParam.TIMESTAMP, ts);
    const logsUrl = `${baseUrl}/${API_URI.LOGS}/stream?${params.toString()}`;

    const headersObj: Record<string, string> = {};
    const headers = this.adminAuth.getAuthHeaders();
    for (const key of headers.keys()) {
      const val = headers.get(key);
      if (val) {
        headersObj[key] = val;
      }
    }

    this.ctrl = new AbortController();
    fetchEventSource(logsUrl, {
      headers: headersObj,
      signal: this.ctrl.signal,
      onmessage: (event: EventSourceMessage) => {
        if (event.data === '') {
          return;
        }
        this._logs.update(lines => {
          const next = [...lines, event.data];
          return next.length > this.MAX_LINES
            ? next.slice(next.length - this.MAX_LINES)
            : next;
        });
      },
      onerror: () => {
        this.disconnect();
      }
    }).catch(() => {
    });
  }

  disconnect(): void {
    this.ctrl?.abort();
    this.ctrl = null;
  }

  clear(): void {
    this._logs.set([]);
  }

  isConnected(): boolean {
    return !!this.ctrl;
  }

  getLogs(taskKey?: string): string[] {
    if (!taskKey) return [];
    return this.taskLogs().get(taskKey) ?? [];
  }

  getLogsSignal(taskKeySignal: () => string | undefined) {
    return computed(() => {
      const taskKey = taskKeySignal();
      if (!taskKey) return [];
      return this.taskLogs().get(taskKey) ?? [];
    });
  }

  hasLogs(taskKey: string): boolean {
    return this.getLogs(taskKey).length > 0;
  }

  connectTask(taskKey: string): void {
    if (this.controllers.has(taskKey)) return;

    const token = this.adminAuth.token;
    if (!token) return;

    let baseUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);
    if (isPlatformBrowser(this.platformId)) {
      baseUrl = this.apiPublicUrl || baseUrl;
    }

    const url = `${baseUrl}/${API_URI.LOGS}/stream/${taskKey}`;
    const ctrl = new AbortController();
    this.controllers.set(taskKey, ctrl);

    fetchEventSource(url, {
      headers: { Authorization: `Bearer ${token}` },
      signal: ctrl.signal,
      onopen: async (response) => {
        if (!response.ok) {
          this.disconnectTask(taskKey);
          throw new Error(`SSE failed: ${response.status}`);
        }
      },
      onmessage: (event) => {
        if (!event.data) return;
        this.taskLogs.update(map => {
          const next = new Map(map);
          next.set(taskKey, [...(next.get(taskKey) ?? []), event.data]);
          return next;
        });
      },
      onerror: (err) => {
        this.disconnectTask(taskKey);
        throw err;
      }
    }).catch(() => {});
  }

  disconnectTask(taskKey: string): void {
    this.controllers.get(taskKey)?.abort();
    this.controllers.delete(taskKey);
  }

  resetTask(taskKey: string): void {
    this.disconnectTask(taskKey);
    this.taskLogs.update(map => {
      const next = new Map(map);
      next.delete(taskKey);
      return next;
    });
  }
}
