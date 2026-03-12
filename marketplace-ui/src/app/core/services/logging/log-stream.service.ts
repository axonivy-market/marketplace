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
import { HttpParams } from '@angular/common/http';
import { RequestParam } from '../../../shared/enums/request-param';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';

@Injectable({
  providedIn: 'root'
})
export class LogStreamService {
  private ctrl: AbortController | null = null;
  private readonly platformId = inject(PLATFORM_ID);
  private readonly runtimeConfig = inject(RuntimeConfigService);
  private readonly adminAuth = inject(AdminAuthService);
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
    headers.keys().forEach(key => {
      const val = headers.get(key);
      if (val) {
        headersObj[key] = val;
      }
    });

    this.ctrl = new AbortController();
    fetchEventSource(logsUrl, {
      headers: headersObj,
      signal: this.ctrl.signal,
      onmessage: (event: any) => {
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
    }).catch(err => {
      // AbortError is expected when we disconnect.
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
}
