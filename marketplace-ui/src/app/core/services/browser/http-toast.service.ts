import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import {
  BAD_GATEWAY,
  BAD_REQUEST_ERROR_CODE,
  FORBIDDEN,
  GATEWAY_TIMEOUT,
  INTERNAL_SERVER_ERROR_CODE,
  NOT_FOUND_ERROR_CODE,
  REQUEST_TIMEOUT,
  SERVICE_UNAVAILABLE,
  UNAUTHORIZED
} from '../../../shared/constants/common.constant';

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
  private readonly errorSubject = new Subject<HttpErrorEvent>();
  private readonly clearSubject = new Subject<void>();
  private readonly dedupeWindow = 1500;
  private lastErrorTime = 0;
  private lastErrorKey = '';

  getError(): Observable<HttpErrorEvent> {
    return this.errorSubject.asObservable();
  }

  getClear(): Observable<void> {
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
      case BAD_REQUEST_ERROR_CODE:
        return 'common.error.description.badRequest';
      case UNAUTHORIZED:
        return 'common.error.description.unauthorized';
      case FORBIDDEN:
        return 'common.error.description.forbidden';
      case NOT_FOUND_ERROR_CODE:
        return 'common.error.description.404';
      case REQUEST_TIMEOUT:
        return 'common.error.description.timeout';
      case INTERNAL_SERVER_ERROR_CODE:
        return 'common.error.description.500';
      case BAD_GATEWAY:
        return 'common.error.description.badGateway';
      case SERVICE_UNAVAILABLE:
        return 'common.error.description.serviceUnavailable';
      case GATEWAY_TIMEOUT:
        return 'common.error.description.gatewayTimeout';
      default:
        return 'common.error.description.default';
    }
  }
}
