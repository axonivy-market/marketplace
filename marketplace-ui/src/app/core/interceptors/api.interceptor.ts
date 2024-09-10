import {
  HttpHeaders,
  HttpContextToken,
  HttpInterceptorFn
} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoadingService } from '../services/loading/loading.service';
import { inject } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const REQUEST_BY = 'X-Requested-By';
export const IVY = 'ivy';
export const ERROR_PAGE_PATH = '/error-page';
export const NOT_FOUND_ERROR_CODE = 404;
export const INTERNAL_SERVER_ERROR_CODE = 500;
export const UNDEFINED_ERROR_CODE = 0;
export const ERROR_CODES = [UNDEFINED_ERROR_CODE, NOT_FOUND_ERROR_CODE, INTERNAL_SERVER_ERROR_CODE];

/** This is option for exclude loading api
 * @Example return httpClient.get('apiEndPoint', { context: new HttpContext().set(SkipLoading, true) })
 */
export const SkipLoading = new HttpContextToken<boolean>(() => false);

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const loadingService = inject(LoadingService);

  if (req.url.includes('i18n')) {
    return next(req);
  }
  let requestURL = req.url;
  const apiURL = environment.apiUrl;
  if (!requestURL.includes(apiURL)) {
    requestURL = `${apiURL}/${req.url}`;
  }

  const cloneReq = req.clone({
    url: requestURL,
    headers: addIvyHeaders(req.headers)
  });

  if (req.context.get(SkipLoading)) {
    return next(cloneReq);
  }

  loadingService.show();

  return next(cloneReq).pipe(
    catchError(error => {
      if (ERROR_CODES.includes(error.status)) {
        router.navigate([ERROR_PAGE_PATH]);
      }
      return throwError(() => new Error(error.message));
    }),
    finalize(() => {
      loadingService.hide();
    })
  );
};

function addIvyHeaders(headers: HttpHeaders): HttpHeaders {
  if (headers.has(REQUEST_BY)) {
    return headers;
  }
  return headers.append(REQUEST_BY, IVY);
}
