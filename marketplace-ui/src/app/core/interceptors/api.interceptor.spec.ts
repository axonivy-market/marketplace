import { HttpClient, HttpHeaders, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { ProductComponent } from '../../modules/product/product.component';
import { DESIGNER_COOKIE_VARIABLE } from '../../shared/constants/common.constant';
import { apiInterceptor } from './api.interceptor';

describe('AuthInterceptor', () => {
  let productComponent: ProductComponent;
  let fixture: ComponentFixture<ProductComponent>;
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ProductComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(withInterceptors([apiInterceptor])),
        HttpTestingController,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({
              [DESIGNER_COOKIE_VARIABLE.restClientParamName]: true
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
});
