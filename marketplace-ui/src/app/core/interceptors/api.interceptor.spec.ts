import {
  HttpClient,
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
import { apiInterceptor, handleHttpError } from './api.interceptor';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';

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
        HttpTestingController,
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
