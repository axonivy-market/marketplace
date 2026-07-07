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
import { CachingEnabled, LoadingComponent } from '../../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { ReleaseLetter } from '../../../shared/models/release-letter-request.model';
import { ReleaseLetterApiResponse } from '../../../shared/models/apis/release-letter-response.model';
import { fail } from 'assert';
import { ReleaseLetterDraftApiResponse } from '../../../shared/models/apis/release-letter-draft-response.model';

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

  describe('getLatestReleaseLetters', () => {
    it('should get latest release letters', () => {
      const mockResponse: ReleaseLetterListApiResponse = {
        _embedded: {
          releaseLetterModelList: [
            {
              sprint: 'S50',
              createdAt: '2024-02-01T00:00:00Z',
              content: 'Latest release content'
            } as any
          ]
        },
        _links: {
          self: { href: '/api/latest-release-letters' }
        }
      } as any;

      service.getLatestReleaseLetters().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url === API_URI.LATEST_RELEASE_LETTERS);

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');
      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);

      req.flush(mockResponse);
    });
  });

  describe('createReleaseLetter', () => {
    it('should create a release letter with ForwardingError context', () => {
      const releaseLetterRequest: ReleaseLetter = {
        sprint: 'S51',
        content: 'New release content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      service.createReleaseLetter(releaseLetterRequest).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(API_URI.RELEASE_LETTERS);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(releaseLetterRequest);
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');

      req.flush(null);
    });
  });

  describe('getReleaseLetterById', () => {
    it('should get release letter by id', () => {
      const id = '123';

      const mockResponse: ReleaseLetterApiResponse = {
        id,
        sprint: 'S52',
        hasDraft: false,
        content: 'Sprint 52 release content',
        createdAt: '2024-02-10T00:00:00Z'
      } as any;

      service.getReleaseLetterById(id).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url === `${API_URI.RELEASE_LETTERS}/${id}`);

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');
      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);

      req.flush(mockResponse);
    });
  });

  describe('deleteReleaseLetterById', () => {
    it('should delete release letter by id', () => {
      const id = '123';

      service.deleteReleaseLetterById(id).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/${id}`);

      expect(req.request.method).toBe('DELETE');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');

      req.flush(null);
    });
  });

  describe('getReleaseLetters', () => {
    it('should call RELEASE_LETTERS with pageable params and default pageId', () => {
      const criteria: ReleaseLetterCriteria = {
        pageable: { page: 0, size: 10 },
        isReadOnly: true
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
        nextPageHref: '/api/release-letters?page=1&size=5',
        isReadOnly: true
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
      const criteria: ReleaseLetterCriteria = {
        isReadOnly: true
      } as any;
      const customPageId = 'CUSTOM_PAGE';

      service.getReleaseLetters(criteria, customPageId).subscribe();

      const req = httpMock.expectOne(req => req.url === API_URI.RELEASE_LETTERS);

      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);
      expect(req.request.context.get(LoadingComponent)).toBe(customPageId);

      req.flush({} as ReleaseLetterListApiResponse);
    });

    it('should return empty object when request fails', () => {
      const criteria: ReleaseLetterCriteria = {
        isReadOnly: true
      } as any;

      service.getReleaseLetters(criteria).subscribe(response => {
        expect(response).toEqual({} as ReleaseLetterListApiResponse);
      });

      const req = httpMock.expectOne(req => req.url === API_URI.RELEASE_LETTERS);

      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);

      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('updateReleaseLetter', () => {
    it('should update release letter by id', () => {
      const selectedId = '123';

      const releaseLetterRequest: ReleaseLetter = {
        id: selectedId,
        sprint: 'S42',
        content: 'Updated content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      const mockResponse: ReleaseLetterApiResponse = {
        id: selectedId,
        sprint: 'S42',
        content: 'Updated content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      service.updateReleaseLetter(selectedId, releaseLetterRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/${selectedId}`);

      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(releaseLetterRequest);
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');

      req.flush(mockResponse);
    });

    it('should propagate error when update release letter fails', () => {
      const selectedId = '123';

      const releaseLetterRequest: ReleaseLetter = {
        id: selectedId,
        sprint: 'S42',
        content: 'Updated content'
      } as any;

      const mockError = {
        status: 500,
        statusText: 'Server Error'
      };

      service.updateReleaseLetter(selectedId, releaseLetterRequest).subscribe({
        next: () => {
          fail('Expected request to fail');
        },
        error: error => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Server Error');
        }
      });

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/${selectedId}`);

      expect(req.request.method).toBe('PUT');

      req.flush('Error updating release letter', mockError);
    });

    it('should call correct endpoint when updating release letter', () => {
      const selectedId = 'release-letter-id';

      service.updateReleaseLetter(selectedId, {} as ReleaseLetter).subscribe();

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/${selectedId}`);

      expect(req.request.url).toBe(`${API_URI.RELEASE_LETTERS}/${selectedId}`);

      req.flush({} as ReleaseLetterApiResponse);
    });
  });

  describe('saveAsDraft', () => {
    it('should save release letter as draft', () => {
      const releaseLetterRequest: ReleaseLetter = {
        id: '123',
        sprint: 'S52',
        content: 'Release content',
        draftContent: 'Draft content',
        createdAt: '2024-02-10T00:00:00Z'
      } as any;

      const mockResponse: ReleaseLetterDraftApiResponse = {
        id: 'draft-123',
        draftContent: 'Draft content'
      } as any;

      service.saveAsDraft(releaseLetterRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/save-as-draft`);

      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(releaseLetterRequest);
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');

      req.flush(mockResponse);
    });

    it('should propagate error when save as draft fails', () => {
      const releaseLetterRequest: ReleaseLetter = {
        id: '123',
        sprint: 'S52',
        draftContent: 'Draft content'
      } as any;

      service.saveAsDraft(releaseLetterRequest).subscribe({
        next: () => {
          fail('Expected request to fail');
        },
        error: error => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Server Error');
        }
      });

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/save-as-draft`);

      expect(req.request.method).toBe('PUT');

      req.flush('Error saving draft', {
        status: 500,
        statusText: 'Server Error'
      });
    });

    it('should call correct endpoint when saving draft', () => {
      service.saveAsDraft({} as ReleaseLetter).subscribe();

      const req = httpMock.expectOne(`${API_URI.RELEASE_LETTERS}/save-as-draft`);

      expect(req.request.url).toBe(`${API_URI.RELEASE_LETTERS}/save-as-draft`);

      req.flush({} as ReleaseLetterDraftApiResponse);
    });
  });

  describe('getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId', () => {
    it('should get release letter draft by release letter id', () => {
      const id = '123';

      const mockResponse: ReleaseLetterDraftApiResponse = {
        id: 'draft-123',
        draftContent: 'Draft content'
      } as any;

      service.getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId(id).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(req => req.url === `${API_URI.RELEASE_LETTERS}/${id}/draft`);

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');
      expect(req.request.params.has(RequestParam.TIMESTAMP)).toBe(true);
      expect(req.request.context.get(CachingEnabled)).toBe(false);

      req.flush(mockResponse);
    });

    it('should return null when no draft exists', () => {
      const id = '123';

      service.getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId(id).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(req => req.url === `${API_URI.RELEASE_LETTERS}/${id}/draft`);

      expect(req.request.method).toBe('GET');

      req.flush(null);
    });

    it('should propagate error when request fails', () => {
      const id = '123';

      service.getReleaseLetterDraftByGitHubUserIdAndReleaseLetterId(id).subscribe({
        next: () => {
          fail('Expected request to fail');
        },
        error: error => {
          expect(error.status).toBe(500);
          expect(error.statusText).toBe('Server Error');
        }
      });

      const req = httpMock.expectOne(req => req.url === `${API_URI.RELEASE_LETTERS}/${id}/draft`);

      expect(req.request.method).toBe('GET');

      req.flush('Error getting draft', {
        status: 500,
        statusText: 'Server Error'
      });
    });
  });
});
