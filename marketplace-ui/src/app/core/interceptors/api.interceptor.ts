import { HttpHeaders, HttpContextToken, HttpInterceptorFn, HttpResponse, HttpStatusCode } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoadingService } from '../services/loading/loading.service';
import { inject, Injector, makeStateKey, PLATFORM_ID, TransferState } from '@angular/core';
import { finalize, of, tap } from 'rxjs';
import { isPlatformServer } from '@angular/common';
import { RuntimeConfigService } from '../configs/runtime-config.service';
import { API_INTERNAL_URL } from '../../shared/constants/api.constant';
import { RUNTIME_CONFIG_KEYS } from '../models/runtime-config';
import { AdminAuthService } from '../../modules/admin-dashboard/admin-auth.service';

export const REQUEST_BY = 'X-Requested-By';
export const IVY = 'marketplace-website';

/** LoadingComponentId: This option for show loading for component which match with id
 * @Example return httpClient.get('apiEndPoint', { context: new HttpContext().set(LoadingComponentId, "detail-page") })
 */
export const LoadingComponent = new HttpContextToken<string>(() => '');

/**
 * CachingEnabled: This option is for enabling caching for GET request.
 * @Example return httpClient.get('apiEndPoint', { context: new HttpContext().set(CachingEnabled, false) })
 */
export const CachingEnabled = new HttpContextToken<boolean>(() => true);

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  const platformId = inject(PLATFORM_ID);
  const transferState = inject(TransferState);
  const key = makeStateKey<unknown>(`${req.method} ${req.urlWithParams}`);

  if (req.url.includes('i18n')) {
    return next(req);
  }

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
  const adminAuthService = inject(AdminAuthService);
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
    headers: addIvyHeaders(req.headers, req.method, adminAuthService.csrfToken()),
    withCredentials: true
  });

  return next(cloneReq).pipe(
    tap(event => {
      if (req.method === 'GET'
        && event instanceof HttpResponse && event.status === HttpStatusCode.Ok
        && req.context.get(CachingEnabled) !== false) {
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

function addIvyHeaders(headers: HttpHeaders, method: string, csrfToken: string | null): HttpHeaders {
  let updatedHeaders = headers;
  if (!updatedHeaders.has(REQUEST_BY)) {
    updatedHeaders = updatedHeaders.append(REQUEST_BY, IVY);
  }
  if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS' && csrfToken && !updatedHeaders.has('X-XSRF-TOKEN')) {
    updatedHeaders = updatedHeaders.append('X-XSRF-TOKEN', csrfToken);
  }
  return updatedHeaders;
}

