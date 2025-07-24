import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExternalDocumentComponent } from './external-document.component';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ROUTER } from '../../constants/router.constant';
import { of } from 'rxjs';
import { ExternalDocument } from '../../models/external-document.model';

describe('ExternalDocumentComponent', () => {
  let component: ExternalDocumentComponent;
  let fixture: ComponentFixture<ExternalDocumentComponent>;
  let httpMock: HttpTestingController;
  let httpClient: jasmine.SpyObj<HttpClient>;
  let router: Router;
  let activatedRoute: ActivatedRoute;

  const mockActivatedRoute = {
    snapshot: {
      paramMap: {
        get: (key: string) => {
          if (key === ROUTER.ID) return 'portal';
          if (key === ROUTER.VERSION) return '10.0';
          return null;
        }
      },
      queryParamMap: {
        get: (key : string) => {
          if (key === ROUTER.REDIRECTED) return 'false';
          return null;
        }
      }
    }
  };

  beforeEach(() => {
    httpClient = jasmine.createSpyObj('HttpClient', ['get']);
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), ExternalDocumentComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: HttpClient, useValue: httpClient }
      ]
    });

    fixture = TestBed.createComponent(ExternalDocumentComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should not redirect if response was empty', () => {
    httpClient.get.and.returnValue(of({} as ExternalDocument));
    fixture.detectChanges();
  });
});
