import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URI } from '../../shared/constants/api.constant';
import { LogFileModel } from '../../shared/models/apis/log-file-response.model';

const BLOB = 'blob';
const RESPONSE = 'response';
const ANCHOR_ELEMENT = 'a';

@Injectable({ providedIn: 'root' })
export class LogService {
  httpClient = inject(HttpClient);

  getLogFiles(date?: string): Observable<LogFileModel[]> {
    let requestURL = API_URI.LOGS;
    let requestParams = new HttpParams();
    requestParams = requestParams.set('date', date ?? '');
    return this.httpClient.get<LogFileModel[]>(requestURL, {
      params: requestParams
    });
  }

  getLogFileContent(fileName: string): void {
    let requestURL = API_URI.LOGS;
    let requestParams = new HttpParams();
    requestParams = requestParams.set('fileName', fileName);
    this.httpClient
      .get(`${requestURL}/download`, {
        params: requestParams,
        responseType: BLOB,
        observe: RESPONSE
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
