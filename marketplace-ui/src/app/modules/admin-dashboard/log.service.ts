import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { LogFileModel } from '../../shared/models/apis/log-file-response.model';
import { AdminAuthService } from './admin-auth.service';

const BLOB = 'blob';
const RESPONSE = 'response';
const ANCHOR_ELEMENT = 'a';
const LOG_BASE_URL = API_URI.LOGS;

@Injectable({ providedIn: 'root' })
export class LogService {
  httpClient = inject(HttpClient);
  adminAuth = inject(AdminAuthService);

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
}
