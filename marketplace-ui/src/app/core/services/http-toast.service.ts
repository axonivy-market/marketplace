import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface HttpErrorEvent {
  status: number;
  messageKey: string;
  url?: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class HttpToastService {
  private errorSubject = new Subject<HttpErrorEvent>();
  private clearSubject = new Subject<void>();
  private lastErrorTime = 0;
  private lastErrorKey = '';
  private dedupeWindow = 1500;

  get error$(): Observable<HttpErrorEvent> {
    return this.errorSubject.asObservable();
  }

  get clear$(): Observable<void> {
    return this.clearSubject.asObservable();
  }

  publishError(error: HttpErrorEvent): void {
    const now = Date.now();
    const isDuplicate = error.messageKey === this.lastErrorKey && now - this.lastErrorTime < this.dedupeWindow;
    if (isDuplicate) {
      return;
    }

    this.lastErrorKey = error.messageKey;
    this.lastErrorTime = now;
    this.errorSubject.next(error);
  }

  clearError(): void {
    this.clearSubject.next();
  }

  /**
   * Map HTTP status code to i18n message key.
   */
  getErrorMessageKey(status: number): string {
    switch (status) {
      case 0:
        return 'common.error.description.networkError';
      case 400:
        return 'common.error.description.badRequest';
      case 401:
        return 'common.error.description.unauthorized';
      case 403:
        return 'common.error.description.forbidden';
      case 404:
        return 'common.error.description.404';
      case 408:
        return 'common.error.description.timeout';
      case 500:
        return 'common.error.description.500';
      case 502:
        return 'common.error.description.badGateway';
      case 503:
        return 'common.error.description.serviceUnavailable';
      case 504:
        return 'common.error.description.gatewayTimeout';
      default:
        return 'common.error.description.default';
    }
  }
}