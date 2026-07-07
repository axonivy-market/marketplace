import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { AdminDashboardService } from './admin-dashboard.service';

describe('AdminDashboardService', () => {
  let service: AdminDashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AdminDashboardService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AdminDashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends write requests without authorization headers', () => {
    service.syncProducts().subscribe();

    const request = httpMock.expectOne(req => req.url === 'api/product/sync' && req.params.get('resetSync') === 'false');
    expect(request.request.method).toBe('PUT');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('loads public custom sort configuration without authorization headers', () => {
    service.getCustomSort().subscribe();

    const request = httpMock.expectOne('api/product-marketplace-data/custom-sort');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });
});
