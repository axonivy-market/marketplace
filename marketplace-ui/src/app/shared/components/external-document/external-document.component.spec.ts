import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ExternalDocumentComponent } from './external-document.component';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

const DOC_API = 'api/product-doc';

describe('ExternalDocumentComponent', () => {
  let component: ExternalDocumentComponent;
  let fixture: any;
  let httpMock: HttpTestingController;
  let mockActivatedRoute: any;

  beforeEach(() => {
    mockActivatedRoute = {
      snapshot: {
        paramMap: {
          get: jasmine.createSpy().and.callFake((param) => {
            switch (param) {
              case 'id': return 'testProduct';
              case 'version': return '1.0';
              default: return null;
            }
          })
        }
      }
    };

    TestBed.configureTestingModule({
      imports: [provideHttpClientTesting, TranslateModule.forRoot()],
      declarations: [ExternalDocumentComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ExternalDocumentComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verify no outstanding HTTP requests
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch document URL and redirect if URLs differ', () => {
    const mockResponse = 'http://new-location.com';
    spyOn(window.location as any, 'href'); // Spy on the window location redirection

    component.ngOnInit();
    const req = httpMock.expectOne(`${DOC_API}/testProduct/1.0`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(window.location.href).toBe(mockResponse); // Expect redirection
  });

  it('should not redirect if response URL matches current URL', () => {
    const currentUrl = window.location.href;
    const mockResponse = currentUrl; // Same URL to test no redirection

    spyOn(window.location as any, 'href'); // Spy on the window location redirection

    component.ngOnInit();
    const req = httpMock.expectOne(`${DOC_API}/testProduct/1.0`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(window.location.href).not.toBe(mockResponse); // No redirection
  });

  it('should handle empty response and not redirect', () => {
    const mockResponse = '';
    spyOn(window.location as any, 'href'); // Spy on the window location redirection

    component.ngOnInit();
    const req = httpMock.expectOne(`${DOC_API}/testProduct/1.0`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(window.location.href).not.toBe(mockResponse); // No redirection
  });

  it('should handle errors from the HTTP request', () => {
    spyOn(console, 'error'); // Spy on the console error

    component.ngOnInit();
    const req = httpMock.expectOne(`${DOC_API}/testProduct/1.0`);
    req.error(new ErrorEvent('Network error'));

    expect(console.error).toHaveBeenCalledWith('Error fetching document URL:', jasmine.any(Error));
  });
});
