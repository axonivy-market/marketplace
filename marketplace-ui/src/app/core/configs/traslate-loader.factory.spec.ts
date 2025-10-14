import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { PLATFORM_ID, TransferState, makeStateKey } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { of } from 'rxjs';
import {
  TranslateUniversalLoader,
  translateUniversalLoaderFactory
} from './translate-loader.factory';
import {
  HttpClient,
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';

const TRANSLATE_KEY = 'translations';
const ASSETS = 'assets';
const I18N = 'i18n';
const JSON_EXTENSION = '.json';

describe('TranslateUniversalLoader', () => {
  let loader: TranslateUniversalLoader;
  let httpMock: HttpTestingController;
  let transferState: TransferState;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TransferState,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'browser' },
        {
          provide: TranslateUniversalLoader,
          useFactory: translateUniversalLoaderFactory,
          deps: [HttpClient, TransferState, PLATFORM_ID]
        }
      ]
    });

    loader = TestBed.inject(TranslateUniversalLoader);
    httpMock = TestBed.inject(HttpTestingController);
    transferState = TestBed.inject(TransferState);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should return cached translations from TransferState', done => {
    const key = makeStateKey<object>(`${TRANSLATE_KEY}-en`);
    const cachedTranslations = { hello: 'world' };
    transferState.set(key, cachedTranslations);

    loader.getTranslation('en').subscribe((result: Object) => {
      expect(result).toEqual(cachedTranslations);
      done();
    });
  });

  it('should load translations from filesystem on the server', done => {
    spyOn(isPlatformServer as any, 'call').and.returnValue(true);
    spyOn<any>(loader, 'loadTranslationsFromFileSystem').and.returnValue(
      of({ hi: 'there' })
    );

    (loader as any).platformId = 'server';

    loader.getTranslation('en').subscribe((result: Object) => {
      expect(result).toEqual({ hi: 'there' });
      done();
    });
  });

  it('should fetch translations via HttpClient on browser', done => {
    spyOn(isPlatformServer as any, 'call').and.returnValue(false);

    const mockResponse = { hello: 'browser' };

    loader.getTranslation('en').subscribe((result: Object) => {
      expect(result).toEqual(mockResponse);
      const key = makeStateKey<object>(`${TRANSLATE_KEY}-en`);
      expect(transferState.get(key, null)).toEqual(mockResponse);
      done();
    });

    const req = httpMock.expectOne(`/${ASSETS}/${I18N}/en${JSON_EXTENSION}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
});
