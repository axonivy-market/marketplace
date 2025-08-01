import { HttpHeaders, HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoadingService } from '../services/loading/loading.service';
import { inject, PLATFORM_ID } from '@angular/core';
import { catchError, EMPTY, finalize } from 'rxjs';
import { Router } from '@angular/router';
import { ERROR_CODES, ERROR_PAGE_PATH } from '../../shared/constants/common.constant';
import { isPlatformServer } from '@angular/common';
import { API_BASE_URL } from '../../shared/constants/api.constant';

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
  const router = inject(Router);
  const loadingService = inject(LoadingService);
  const platformId = inject(PLATFORM_ID);

  if (req.url.includes('i18n')) {
    return next(req);
  }

  if (req.context.get(LoadingComponent)) {
    loadingService.showLoading(req.context.get(LoadingComponent));
  }

  let apiURL = environment.apiUrl;
  if (isPlatformServer(platformId)) {
    try {
      apiURL = inject(API_BASE_URL);
    } catch (e) {
      console.error('SSR Interceptor ERROR: Could not inject API_BASE_URL: ', e);
    }
  }
  let requestURL = req.url;
  if (!requestURL.includes(apiURL)) {
    requestURL = `${apiURL}/${req.url}`;
  }

  const cloneReq = req.clone({
    url: requestURL,
    headers: addIvyHeaders(req.headers)
  });

  if (req.context.get(ForwardingError)) {
    return next(cloneReq);
  }
  
  return next(cloneReq).pipe(
    catchError(error => {
      if (ERROR_CODES.includes(error.status)) {
        router.navigate([`${ERROR_PAGE_PATH}/${error.status}`]);
      } else {
        router.navigate([ERROR_PAGE_PATH]);
      }
      return EMPTY;
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