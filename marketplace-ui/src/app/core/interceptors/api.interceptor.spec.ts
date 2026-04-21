import { afterEach, beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import {
  HttpClient,
  HttpContext,
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
import { of } from 'rxjs';
import { ProductComponent } from '../../modules/product/product.component';
import { DESIGNER_SESSION_STORAGE_VARIABLE } from '../../shared/constants/common.constant';
import { apiInterceptor, CACHING_ENABLED } from './api.interceptor';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { makeStateKey, PLATFORM_ID, TransferState } from '@angular/core';
import { API_INTERNAL_URL } from '../../shared/constants/api.constant';
import { RuntimeConfigService } from '../configs/runtime-config.service';
import { LoadingService } from '../services/loading/loading.service';

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

  describe('apiInterceptor - CACHING_ENABLED', () => {
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
            useValue: { get: () => '/app' }
          },
          {
            provide: LoadingService,
            useValue: { showLoading: () => {}, hideLoading: () => {} }
          }
        ]
      });

      http = TestBed.inject(HttpClient);
      httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
      httpMock.verify();
    });

    it('should NOT cache GET response when CACHING_ENABLED is false', () => {
      const transferState = TestBed.inject(TransferState);
      const url = 'product';
      const body = { id: 1 };
      const context = new HttpContext().set(CACHING_ENABLED, false);

      http.get(url, { context }).subscribe();

      const req = httpMock.expectOne('/app/product');
      req.flush(body);

      const key = makeStateKey<any>(`GET ${url}`);
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

});
