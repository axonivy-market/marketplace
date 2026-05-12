import { afterEach, beforeEach, describe, expect, it, MockedObject, vi } from 'vitest';
import { NewsManagementService } from './news-management.service';
import { AdminAuthService } from '../admin-auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpHeaders } from '@angular/common/http';
import { AUTHORIZATION_HEADER } from '../../../shared/constants/common.constant';
import { TestBed } from '@angular/core/testing';
import { ReleaseLetterCriteria } from '../../../shared/models/criteria.model';
import { ReleaseLetterListApiResponse } from '../../../shared/models/apis/release-letter-list-response.model';
import { API_URI } from '../../../shared/constants/api.constant';
import { RequestParam } from '../../../shared/enums/request-param';
import { LoadingComponent } from '../../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';

describe('NewsManagementService', () => {
  let service: NewsManagementService;
  let httpMock: HttpTestingController;
  let adminAuthService: MockedObject<AdminAuthService>;

  const mockAuthHeaders = new HttpHeaders({
    [AUTHORIZATION_HEADER]: 'Bearer test-token'
  });

  beforeEach(() => {
    adminAuthService = {
      getAuthHeaders: vi.fn().mockName('AdminAuthService.getAuthHeaders')
    } as MockedObject<AdminAuthService>;
    adminAuthService.getAuthHeaders.mockReturnValue(mockAuthHeaders);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        NewsManagementService,
        { provide: AdminAuthService, useValue: adminAuthService }
      ]
    });

    service = TestBed.inject(NewsManagementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getReleaseLetters', () => {
    it('should call RELEASE_LETTERS with pageable params and default pageId', () => {
      const criteria: ReleaseLetterCriteria = {
        pageable: { page: 0, size: 10 }
      } as any;

      const mockResponse: ReleaseLetterListApiResponse = {
        _embedded: { releaseLetterModelList: [] }
      } as any;

      service.getReleaseLetters(criteria).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        request =>
          request.url === API_URI.RELEASE_LETTERS &&
          request.params.get(RequestParam.PAGE) === '0' &&
          request.params.get(RequestParam.SIZE) === '10'
      );

      expect(req.request.method).toBe('GET');

      expect(req.request.context.get(LoadingComponent)).toBe(LoadingComponentId.NEWS_PAGE);

      req.flush(mockResponse);
    });

    it('should call nextPageHref when provided', () => {
      const criteria: ReleaseLetterCriteria = {
        nextPageHref: '/api/release-letters?page=1&size=5'
      } as any;

      const mockResponse = {} as ReleaseLetterListApiResponse;

      service.getReleaseLetters(criteria).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url === '/api/release-letters?page=1&size=5');

      expect(req.request.method).toBe('GET');
      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);

      req.flush(mockResponse);
    });

    it('should use custom pageId in HttpContext', () => {
      const criteria: ReleaseLetterCriteria = {} as any;
      const customPageId = 'CUSTOM_PAGE';

      service.getReleaseLetters(criteria, customPageId).subscribe();

      const req = httpMock.expectOne(req => req.url === API_URI.RELEASE_LETTERS);

      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);
      expect(req.request.context.get(LoadingComponent)).toBe(customPageId);

      req.flush({} as ReleaseLetterListApiResponse);
    });

    it('should return empty object when request fails', () => {
      const criteria: ReleaseLetterCriteria = {} as any;

      service.getReleaseLetters(criteria).subscribe(response => {
        expect(response).toEqual({} as ReleaseLetterListApiResponse);
      });

      const req = httpMock.expectOne(req => req.url === API_URI.RELEASE_LETTERS);

      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);

      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });
});
