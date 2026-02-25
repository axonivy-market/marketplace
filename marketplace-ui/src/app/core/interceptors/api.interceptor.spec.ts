import {
  HttpClient,
  HttpContext,
  HttpErrorResponse,
  HttpHeaders,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { EMPTY, of } from 'rxjs';
import { ProductComponent } from '../../modules/product/product.component';
import {
  DESIGNER_SESSION_STORAGE_VARIABLE,
  ERROR_CODES,
  ERROR_PAGE_PATH,
  UNAUTHORIZED
} from '../../shared/constants/common.constant';
import {
  apiInterceptor,
  ForwardingError,
  handleHttpError,
  invalidateGetCache
} from './api.interceptor';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { makeStateKey, PLATFORM_ID, TransferState } from '@angular/core';
import { API_INTERNAL_URL } from '../../shared/constants/api.constant';
import { RuntimeConfigService } from '../configs/runtime-config.service';
import { LoadingService } from '../services/loading/loading.service';

describe('AuthInterceptor', () => {
  let mockRouter: jasmine.SpyObj<Router>;
  let productComponent: ProductComponent;
  let fixture: ComponentFixture<ProductComponent>;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ProductComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({
              [DESIGNER_SESSION_STORAGE_VARIABLE.restClientParamName]: true
            })
          }
        }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(ProductComponent);
    productComponent = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should throw error', () => {
    const headers = new HttpHeaders({
      'X-Requested-By': 'ivy'
    });
    httpClient.get('product', { headers }).subscribe({
      next() {
        fail('Expected an error, but got a response');
      },
      error(e) {
        expect(e.status).not.toBe(200);
      }
    });
  });

  it('should throw error with the url contains i18n', () => {
    httpClient.get('assets/i18n').subscribe({
      next() {
        fail('Expected an error, but got a response');
      },
      error(e) {
        expect(e.status).not.toBe(200);
      }
    });
  });

  it('should return cached GET response from TransferState and not call backend', () => {
    const transferState = TestBed.inject(TransferState);

    const url = 'product';
    const expectedBody = { name: 'cached-product' };

    const key = makeStateKey<unknown>(`GET ${url}`);

    // Pre-populate TransferState
    transferState.set(key, expectedBody);

    let actualResponse: any;

    httpClient.get(url).subscribe(response => {
      actualResponse = response;
    });

    httpTestingController.expectNone(`${url}`);
    expect(actualResponse).toEqual(expectedBody);
    expect(transferState.hasKey(key)).toBeFalse();
  });

  it('should use API_INTERNAL_URL when running on server', () => {
    const internalUrl = 'http://internal-api';

    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'server' },
        { provide: API_INTERNAL_URL, useValue: internalUrl },
        {
          provide: RuntimeConfigService,
          useValue: {
            get: () => 'http://public-api'
          }
        },
        {
          provide: LoadingService,
          useValue: {
            showLoading: () => {},
            hideLoading: () => {}
          }
        }
      ]
    });

    const http = TestBed.inject(HttpClient);
    const httpMock = TestBed.inject(HttpTestingController);

    http.get('product').subscribe();

    const req = httpMock.expectOne(`${internalUrl}/product`);

    expect(req.request.url).toBe(`${internalUrl}/product`);

    req.flush({});
  });

  describe('apiInterceptor - non-GET branch', () => {
    let http: HttpClient;
    let httpMock: HttpTestingController;

    beforeEach(() => {
      TestBed.resetTestingModule();

      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(withInterceptors([apiInterceptor])),
          provideHttpClientTesting(),
          {
            provide: RuntimeConfigService,
            useValue: { get: () => '/app' } // match real behavior
          },
          {
            provide: LoadingService,
            useValue: {
              showLoading: () => {},
              hideLoading: () => {}
            }
          },
          {
            provide: Router,
            useValue: jasmine.createSpyObj('Router', ['navigate'])
          }
        ]
      });

      http = TestBed.inject(HttpClient);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should invalidate GET cache when non-GET request succeeds', () => {
      const transferState = TestBed.inject(TransferState);

      const url = 'product';

      const key = makeStateKey<any>(`GET ${url}`);
      transferState.set(key, { old: 'data' });

      http.post(url, { name: 'new' }).subscribe();

      const req = httpMock.expectOne('/app/product');
      req.flush({}, { status: 200, statusText: 'OK' });

      expect(transferState.hasKey(key)).toBeFalse();
    });
  });

  it('should cache GET response when status is 200 and not release letters API', () => {
    const transferState = TestBed.inject(TransferState);
    const http = TestBed.inject(HttpClient);
    const httpMock = TestBed.inject(HttpTestingController);

    const url = 'product';
    const body = { id: 1 };

    http.get(url).subscribe();

    const req = httpMock.expectOne('/app/product');
    req.flush(body);

    const key = makeStateKey<any>(`GET ${url}`);

    expect(transferState.get(key, null)).toEqual(body);
  });

  it('should call handleHttpError and navigate on server error', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    const http = TestBed.inject(HttpClient);
    const httpMock = TestBed.inject(HttpTestingController);

    http.get('product').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/app/product');

    req.flush({}, { status: 500, statusText: 'Server Error' });

    expect(router.navigate).toHaveBeenCalled();
  });

  it('should remove GET cache when key exists', () => {
    const transferState = TestBed.inject(TransferState);

    const url = 'product';

    const key = makeStateKey<any>(`GET ${url}`);
    transferState.set(key, { id: 1 });

    expect(transferState.hasKey(key)).toBeTrue();
    invalidateGetCache(transferState, url);
    expect(transferState.hasKey(key)).toBeFalse();
  });

  it('should not throw or remove anything when key does not exist', () => {
    const transferState = TestBed.inject(TransferState);

    const url = 'non-existing';

    const key = makeStateKey<any>(`GET ${url}`);

    expect(transferState.hasKey(key)).toBeFalse();
    invalidateGetCache(transferState, url);
    expect(transferState.hasKey(key)).toBeFalse();
  });

  describe('apiInterceptor - ForwardingError branch', () => {
    let http: HttpClient;
    let httpMock: HttpTestingController;

    beforeEach(() => {
      TestBed.resetTestingModule();

      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(withInterceptors([apiInterceptor])),
          provideHttpClientTesting(),
          {
            provide: RuntimeConfigService,
            useValue: {
              get: () => 'http://api'
            }
          },
          {
            provide: LoadingService,
            useValue: {
              showLoading: () => {},
              hideLoading: () => {}
            }
          }
        ]
      });

      http = TestBed.inject(HttpClient);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify(); // ensures no unexpected requests
    });

    it('should cache GET response when ForwardingError is true', () => {
      const transferState = TestBed.inject(TransferState);

      const url = 'product';
      const body = { id: 1 };

      const context = new HttpContext().set(ForwardingError, true);

      http.get(url, { context }).subscribe();

      const req = httpMock.expectOne('http://api/product');

      req.flush(body);

      const key = makeStateKey<unknown>(`GET ${url}`);

      expect(transferState.get(key, null)).toEqual(body);
    });
  });

  describe('handleHttpError', () => {
    beforeEach(() => {
      mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);
    });

    it('should throw error if status is UNAUTHORIZED', done => {
      const error = new HttpErrorResponse({ status: UNAUTHORIZED });

      handleHttpError(mockRouter, error).subscribe({
        error: (err: HttpErrorResponse) => {
          expect(err).toBe(error);
          expect(mockRouter.navigate).not.toHaveBeenCalled();
          done();
        }
      });
    });

    it('should navigate to specific error page if status is in ERROR_CODES', () => {
      const error = new HttpErrorResponse({ status: ERROR_CODES[0] });

      const result = handleHttpError(mockRouter, error);

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        `${ERROR_PAGE_PATH}/${error.status}`
      ]);
      expect(result).toBe(EMPTY);
    });

    it('should navigate to generic error page if status not in ERROR_CODES', () => {
      const error = new HttpErrorResponse({ status: 418 });

      const result = handleHttpError(mockRouter, error);

      expect(mockRouter.navigate).toHaveBeenCalledWith([ERROR_PAGE_PATH]);
      expect(result).toBe(EMPTY);
    });
  });
});
