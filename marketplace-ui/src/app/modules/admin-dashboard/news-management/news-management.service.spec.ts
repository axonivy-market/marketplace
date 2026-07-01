import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { NewsManagementService } from './news-management.service';

describe('NewsManagementService', () => {
  let service: NewsManagementService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NewsManagementService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(NewsManagementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads release letters without authorization headers', () => {
    service.getLatestReleaseLetters().subscribe();

    const request = httpMock.expectOne(req => req.url === 'api/release-letters/latest');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('creates release letters using the session cookie flow', () => {
    service.createReleaseLetter({} as any).subscribe();

    const request = httpMock.expectOne('api/release-letters');
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });
});
