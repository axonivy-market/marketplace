import {
  HttpHeaders,
  HttpContextToken,
  HttpInterceptorFn,
  HttpResponse,
  HttpStatusCode,
  HttpErrorResponse
} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoadingService } from '../services/loading/loading.service';
import { inject, Injector, makeStateKey, PLATFORM_ID, TransferState } from '@angular/core';
import { catchError, EMPTY, finalize, Observable, of, tap, throwError } from 'rxjs';
import { FORBIDDEN, UNAUTHORIZED } from '../../shared/constants/common.constant';
import { isPlatformServer } from '@angular/common';
import { RuntimeConfigService } from '../configs/runtime-config.service';
import { API_INTERNAL_URL, API_URI } from '../../shared/constants/api.constant';
import { RUNTIME_CONFIG_KEYS } from '../models/runtime-config';
import { HttpErrorBusService } from '../services/http-error-bus.service';

export const REQUEST_BY = 'X-Requested-By';
export const IVY = 'marketplace-website';

/** ForwardingError: This option for forwarding responce error to the caller
 * @Example return httpClient.get('apiEndPoint', { context: new HttpContext().set(ForwardingError, true) })
 */
export const ForwardingError = new HttpContextToken<boolean>(() => false);

/** LoadingComponentId: This option for show loading for component which match with id
 * @Example return httpClient.get('apiEndPoint', { context: new HttpContext().set(LoadingComponentId, "detail-page") })
 */
export const LoadingComponent = new HttpContextToken<string>(() => '');

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  const platformId = inject(PLATFORM_ID);
  const transferState = inject(TransferState);
  const errorBus = inject(HttpErrorBusService);
  const key = makeStateKey<unknown>(`${req.method} ${req.urlWithParams}`);

  if (req.url.includes('i18n')) {
    return next(req);
  }

  const isReleaseLettersApi = req.url.includes(API_URI.RELEASE_LETTERS);

  // Only cache GET requests to API
  if (req.method === 'GET' && transferState.hasKey(key)) {
    const data = transferState.get<unknown>(key, null);
    transferState.remove(key);
    return of(new HttpResponse({ body: data, status: HttpStatusCode.Ok }));
  }

  if (req.context.get(LoadingComponent)) {
    loadingService.showLoading(req.context.get(LoadingComponent));
  }

  const injector = inject(Injector);
  const runtimeConfig = inject(RuntimeConfigService);
  let apiURL = runtimeConfig.get(RUNTIME_CONFIG_KEYS.MARKET_API_URL);

  if (isPlatformServer(platformId)) {
    apiURL = injector.get(API_INTERNAL_URL, environment.apiInternalUrl);
  }

  let requestURL = req.url;
  if (!requestURL.includes(`${apiURL}/`)) {
    requestURL = `${apiURL}/${req.url}`;
  }
  
  const cloneReq = req.clone({
    url: requestURL,
    headers: addIvyHeaders(req.headers)
  });

  if (req.context.get(ForwardingError)) {
    return next(cloneReq).pipe(
      tap(event => {
        if (
          req.method === 'GET' &&
          !isReleaseLettersApi &&
          event instanceof HttpResponse &&
          event.status === HttpStatusCode.Ok
        ) {
          transferState.set(key, event.body);
        }
      })
    );
  }

  return next(cloneReq).pipe(
    catchError(error => handleHttpError(errorBus, error, req.url)),
    tap(event => {
      if (
        event instanceof HttpResponse &&
        event.status === HttpStatusCode.Ok &&
        req.method !== 'GET'
      ) {
        invalidateGetCache(transferState, req.urlWithParams);
      }
      if (
        req.method === 'GET' &&
        !isReleaseLettersApi &&
        event instanceof HttpResponse &&
        event.status === HttpStatusCode.Ok
      ) {
        transferState.set(key, event.body);
      }
    }),
    finalize(() => {
      if (req.context.get(LoadingComponent)) {
        loadingService.hideLoading(req.context.get(LoadingComponent));
      }
    })
  );
};

function addIvyHeaders(headers: HttpHeaders): HttpHeaders {
  if (headers.has(REQUEST_BY)) {
    return headers;
  }
  return headers.append(REQUEST_BY, IVY);
}

export function handleHttpError(
  errorBus: HttpErrorBusService,
  error: HttpErrorResponse,
  url: string
): Observable<never> {
  // Keep auth errors for caller to handle (e.g., auth service, guards)
  if (error.status === UNAUTHORIZED || error.status === FORBIDDEN) {
    return throwError(() => error);
  }

  // Publish non-auth errors to global error bus so they show as a banner
  const messageKey = errorBus.getErrorMessageKey(error.status);
  errorBus.publishError({
    status: error.status,
    messageKey,
    url,
    timestamp: Date.now()
  });

  return EMPTY;
}

export function invalidateGetCache(transferState: TransferState, url: string) {
  const key = makeStateKey(`GET ${url}`);
  if (transferState.hasKey(key)) {
    transferState.remove(key);
  }
}
