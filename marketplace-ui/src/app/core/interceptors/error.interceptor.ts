import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { LoadingService } from '../services/loading/loading.service';
import { HttpToastService, HttpErrorEvent } from '../services/browser/http-toast.service';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, finalize, Observable, throwError } from 'rxjs';
import { LoadingComponent } from './api.interceptor';
import { FORBIDDEN, UNAUTHORIZED, UNDEFINED_ERROR_CODE } from '../../shared/constants/common.constant';
import { AdminAuthService } from '../../modules/admin-dashboard/admin-auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  const toastService = inject(HttpToastService);
  const platformId = inject(PLATFORM_ID);
  const router = inject(Router);
  const adminAuthService = inject(AdminAuthService);

  return next(req).pipe(
    catchError(error => {
      if (error instanceof HttpErrorResponse) {
        return handleHttpError(
          toastService,
          error,
          req.url,
          req.method,
          router,
          adminAuthService,
          isPlatformBrowser(platformId)
        );
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
  url: string, method: string, router: Router, adminAuthService: AdminAuthService, isBrowser = true): Observable<never> {
  // Keep auth errors for caller to handle (e.g., auth service, guards)
  if (error.status === UNAUTHORIZED || error.status === FORBIDDEN) {
    if (isBrowser && error.status === UNAUTHORIZED && method !== 'GET' && !url.includes('auth/admin/v2/github/callback')) {
      adminAuthService.clearToken();
      router.navigate(['/admin-login-v2']);
    }
    return throwError(() => error);
  }

  if (isBrowser) {
    let messageKey = 'common.error.description.default';
    if (error.status !== UNDEFINED_ERROR_CODE) {
      messageKey = error.error?.messageDetails || toastService.getErrorMessageKey(error.status);
    }
    const httpErrorEvent: HttpErrorEvent = {
      status: error.status,
      timestamp: Date.now(),
      messageKey,
      url
    };
    toastService.publishError(httpErrorEvent);
  }

  return throwError(() => error);
}
