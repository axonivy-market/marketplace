import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ROUTER } from '../../constants/router.constant';
import { HttpClient, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { RedirectPageComponent } from './redirect-page.component';
import { of } from 'rxjs';
import { API_URI } from '../../constants/api.constant';
import { MOCK_EXTERNAL_DOCUMENT } from '../../mocks/mock-data';

describe('ExternalDocumentComponent', () => {
  let component: RedirectPageComponent;
  let fixture: any;
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
      }
    }
  };

  beforeEach(() => {
    httpClient = jasmine.createSpyObj('HttpClient', ['get']);
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), RedirectPageComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: HttpClient, useValue: httpClient }
      ]
    });

    fixture = TestBed.createComponent(RedirectPageComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should not redirect if response URL matches current URL', () => {
    const currentUrl = window.location.href;
    let mockResponse = { ...MOCK_EXTERNAL_DOCUMENT };
    mockResponse.relativeLink = currentUrl;
    httpClient.get.and.returnValue(of(mockResponse));

    component.ngOnInit();

    expect(httpClient.get).toHaveBeenCalledWith(`${API_URI.EXTERNAL_DOCUMENT}/portal/10.0`);
    expect(window.location.href).toBe(currentUrl);
  });
});
