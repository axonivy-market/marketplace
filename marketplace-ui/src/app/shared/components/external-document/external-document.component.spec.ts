import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ROUTER } from '../../constants/router.constant';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ExternalDocumentComponent } from './external-document.component';

describe('ExternalDocumentComponent', () => {
  let component: ExternalDocumentComponent;
  let fixture: any;
  let httpMock: HttpTestingController;
  let router: Router;
  let activatedRoute: ActivatedRoute;

  const mockActivatedRoute = {
    snapshot: {
      paramMap: {
        get: (key: string) => {
          if (key === ROUTER.ID) return 'testProduct';
          if (key === ROUTER.VERSION) return '1.0';
          return null;
        }
      }
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot(),
        ExternalDocumentComponent
      ],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    });

    fixture = TestBed.createComponent(ExternalDocumentComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
    httpMock.verify();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

});
