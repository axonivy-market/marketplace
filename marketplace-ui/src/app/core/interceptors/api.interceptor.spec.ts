import { afterEach, beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
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
import { HttpErrorBusService } from '../services/http-error-bus.service';

describe('AuthInterceptor', () => {
  let mockRouter: MockedObject<Router>;
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
        throw new Error('Expected an error, but got a response');
      },
      error(e) {
        expect(e.status).not.toBe(200);
      }
    });
  });

  it('should throw error with the url contains i18n', () => {
    httpClient.get('assets/i18n').subscribe({
      next() {
        throw new Error('Expected an error, but got a response');
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
    expect(transferState.hasKey(key)).toBe(false);
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
            useValue: {
              navigate: vi.fn().mockName('Router.navigate')
            }
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

      expect(transferState.hasKey(key)).toBe(false);
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

  it('should publish global error and not navigate on server error', () => {
    const errorBus = TestBed.inject(HttpErrorBusService);
    const publishSpy = vi.spyOn(errorBus, 'publishError');

    const http = TestBed.inject(HttpClient);
    const httpMock = TestBed.inject(HttpTestingController);

    http.get('product').subscribe({
      error: () => {}
    });

    const req = httpMock.expectOne('/app/product');

    req.flush({}, { status: 500, statusText: 'Server Error' });

    expect(publishSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        status: 500,
        url: 'product'
      })
    );
  });

  it('should remove GET cache when key exists', () => {
    const transferState = TestBed.inject(TransferState);

    const url = 'product';

    const key = makeStateKey<any>(`GET ${url}`);
    transferState.set(key, { id: 1 });

    expect(transferState.hasKey(key)).toBe(true);
    invalidateGetCache(transferState, url);
    expect(transferState.hasKey(key)).toBe(false);
  });

  it('should not throw or remove anything when key does not exist', () => {
    const transferState = TestBed.inject(TransferState);

    const url = 'non-existing';

    const key = makeStateKey<any>(`GET ${url}`);

    expect(transferState.hasKey(key)).toBe(false);
    invalidateGetCache(transferState, url);
    expect(transferState.hasKey(key)).toBe(false);
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
    let errorBusService: MockedObject<HttpErrorBusService>;

    beforeEach(() => {
      errorBusService = {
        publishError: vi.fn().mockName('HttpErrorBusService.publishError'),
        getErrorMessageKey: vi.fn().mockName('HttpErrorBusService.getErrorMessageKey')
      } as unknown as MockedObject<HttpErrorBusService>;
    });

    it('should throw error if status is UNAUTHORIZED', async () => {
      const error = new HttpErrorResponse({ status: UNAUTHORIZED });

      handleHttpError(errorBusService, error, '/test-url').subscribe({
        error: (err: HttpErrorResponse) => {
          expect(err).toBe(error);
          expect(errorBusService.publishError).not.toHaveBeenCalled();
        }
      });
    });

    it('should publish error to error bus for non-auth errors', () => {
      const error = new HttpErrorResponse({ status: 500 });
      const messageKey = 'common.error.description.500';
      (errorBusService.getErrorMessageKey as any).mockReturnValue(messageKey);

      const result = handleHttpError(errorBusService, error, '/test-url');

      expect(errorBusService.getErrorMessageKey).toHaveBeenCalledWith(500);
      expect(errorBusService.publishError).toHaveBeenCalledWith({
        status: 500,
        messageKey,
        url: '/test-url',
        timestamp: expect.any(Number)
      });
      expect(result).toBe(EMPTY);
    });

    it('should publish error for 404 status', () => {
      const error = new HttpErrorResponse({ status: 404 });
      const messageKey = 'common.error.description.404';
      (errorBusService.getErrorMessageKey as any).mockReturnValue(messageKey);

      handleHttpError(errorBusService, error, '/product/not-found');

      expect(errorBusService.publishError).toHaveBeenCalledWith(
        expect.objectContaining({
          status: 404,
          messageKey,
          url: '/product/not-found'
        })
      );
    });
  });
});
