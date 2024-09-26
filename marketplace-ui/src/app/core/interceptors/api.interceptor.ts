import {
  HttpHeaders,
  HttpContextToken,
  HttpInterceptorFn
} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoadingService } from '../services/loading/loading.service';
import { inject } from '@angular/core';
import { catchError, EMPTY, finalize } from 'rxjs';
import { Router } from '@angular/router';
import { ERROR_CODES, ERROR_PAGE_PATH } from '../../shared/constants/common.constant';

export const REQUEST_BY = 'X-Requested-By';
export const IVY = 'marketplace-website';

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
        router.navigate([`${ERROR_PAGE_PATH}/error.status`]);
      } else {
        router.navigate([ERROR_PAGE_PATH]);
      }
      return EMPTY;
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