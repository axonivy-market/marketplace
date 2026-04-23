import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { LoadingService } from '../services/loading/loading.service';
import { HttpToastService, HttpErrorEvent } from '../services/browser/http-toast.service';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { catchError, finalize, Observable, throwError } from 'rxjs';
import { LoadingComponent } from './api.interceptor';
import { FORBIDDEN, UNAUTHORIZED } from '../../shared/constants/common.constant';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  const toastService = inject(HttpToastService);
  const platformId = inject(PLATFORM_ID);

  return next(req).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse) {
        return handleHttpError(toastService, error, req.url, isPlatformBrowser(platformId));
      }
      return throwError(() => error);
    }),
    finalize(() => {
      if (req.context.get(LoadingComponent)) {
        loadingService.hideLoading(req.context.get(LoadingComponent));
      }
    })
  );
};

export function handleHttpError(toastService: HttpToastService, error: HttpErrorResponse,
  url: string, isBrowser: boolean): Observable<never> {
  // Keep auth errors for caller to handle (e.g., auth service, guards)
  if (error.status === UNAUTHORIZED || error.status === FORBIDDEN) {
    return throwError(() => error);
  }

  if (isBrowser) {
    const messageKey = toastService.getErrorMessageKey(error.status);
    const httpErrorEvent: HttpErrorEvent = {
      status: error.status,
      messageKey: error?.error?.messageDetails || messageKey,
      timestamp: Date.now(),
      url
    };
    toastService.publishError(httpErrorEvent);
  }

  return throwError(() => error);
}
