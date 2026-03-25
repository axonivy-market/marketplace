import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject, computed } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { LogFileModel } from '../../shared/models/apis/log-file-response.model';
import { AdminAuthService } from './admin-auth.service';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { signal } from '@angular/core';
import { RuntimeConfigService } from '../../core/configs/runtime-config.service';
import { isPlatformBrowser } from '@angular/common';
import { RUNTIME_CONFIG_KEYS } from '../../core/models/runtime-config';
import { API_PUBLIC_URL } from '../../shared/constants/api.constant';
import { PLATFORM_ID } from '@angular/core';

const BLOB = 'blob';
const RESPONSE = 'response';
const ANCHOR_ELEMENT = 'a';
const LOG_BASE_URL = API_URI.LOGS;

@Injectable({ providedIn: 'root' })
export class LogService {
  httpClient = inject(HttpClient);
  adminAuth = inject(AdminAuthService);
  private readonly taskLogs = signal<Map<string, string[]>>(new Map());
  private controllers = new Map<string, AbortController>();
  private readonly platformId = inject(PLATFORM_ID);
  private readonly runtimeConfig = inject(RuntimeConfigService);
  private readonly apiPublicUrl = inject(API_PUBLIC_URL, { optional: true });

  getLogFiles(date?: string): Observable<LogFileModel[]> {
    let requestParams = new HttpParams();
    requestParams = requestParams.set('date', date ?? '');
    return this.httpClient.get<LogFileModel[]>(LOG_BASE_URL, {
      params: requestParams,
      headers: this.adminAuth.getAuthHeaders()
    });
  }

  getLogFileContent(fileName: string): void {
    let requestParams = new HttpParams();
    requestParams = requestParams.set('fileName', fileName);
    this.httpClient
      .get(`${LOG_BASE_URL}/download`, {
        params: requestParams,
        responseType: BLOB,
        observe: RESPONSE,
        headers: this.adminAuth.getAuthHeaders()
      })
      .subscribe({
        next: (response: HttpResponse<Blob>) => {
          if (response.body) {
            this.triggerDownload(response.body, fileName);
          }
        },
        error: () => {}
      });
  }

  triggerDownload(blob: Blob, fileName: string): void {
    const downloadUrl = URL.createObjectURL(blob);
    const anchor = document.createElement(ANCHOR_ELEMENT);
    anchor.href = downloadUrl;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(downloadUrl);
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

  connect(taskKey: string): void {
    if (this.controllers.has(taskKey)) return;

    const token = this.adminAuth.token;
    if (!token) return;

    let baseUrl = this.runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);
    if (isPlatformBrowser(this.platformId)) {
      baseUrl = this.apiPublicUrl || baseUrl;
    }

    const url = `${baseUrl}/${API_URI.LOGS}/stream/${taskKey}`;
    console.log('SSE URL:', url);

    const ctrl = new AbortController();
    this.controllers.set(taskKey, ctrl);

    fetchEventSource(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      signal: ctrl.signal,
      onopen: async (response) => {
        if (!response.ok) {
          this.disconnect(taskKey);
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
        this.disconnect(taskKey);
        throw err;
      }
    }).catch(() => {});
  }

  disconnect(taskKey: string): void {
    this.controllers.get(taskKey)?.abort();
    this.controllers.delete(taskKey);
  }

  reset(taskKey: string): void {
    this.disconnect(taskKey);
    console.log('Resetting logs for:', taskKey);
    this.taskLogs.update(map => {
      const next = new Map(map);
      next.delete(taskKey);
      return next;
    });
  }
}
