import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {
  AdminDashboardService,
  SyncTaskExecution
} from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';
import { API_URI } from '../../shared/constants/api.constant';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { HttpHeaders } from '@angular/common/http';
import { ReleaseLetterListApiResponse } from '../../shared/models/apis/release-letter-list-response.model';
import { ReleaseLetterCriteria } from '../../shared/models/criteria.model';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import {
  ForwardingError,
  LoadingComponent
} from '../../core/interceptors/api.interceptor';
import { ReleaseLetter } from '../../shared/models/release-letter-request.model';
import { ReleaseLetterApiResponse } from '../../shared/models/apis/release-letter-response.model';

describe('AdminDashboardService', () => {
  let service: AdminDashboardService;
  let httpMock: HttpTestingController;
  let adminAuthService: jasmine.SpyObj<AdminAuthService>;

  const mockAuthHeaders = new HttpHeaders({ 'Authorization': 'Bearer test-token' });

  beforeEach(() => {
    adminAuthService = jasmine.createSpyObj('AdminAuthService', ['getAuthHeaders']);
    adminAuthService.getAuthHeaders.and.returnValue(mockAuthHeaders);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AdminDashboardService,
        { provide: AdminAuthService, useValue: adminAuthService }
      ]
    });

    service = TestBed.inject(AdminDashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('syncProducts', () => {
    it('should sync products with resetSync=true', () => {
      const mockResponse: SyncTaskExecution = {
        key: 'syncProducts',
        status: undefined,
        triggeredAt: '2024-01-01T00:00:00Z'
      };

      service.syncProducts(true).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request => 
        request.url === `${API_URI.PRODUCT}/sync` && 
        request.params.get(RequestParam.RESET_SYNC) === 'true'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });

    it('should sync one product with overrideMarketItemPath=true', () => {
      const mockResponse: SyncTaskExecution = {
        key: 'syncOneProduct',
        status: undefined,
        triggeredAt: '2024-01-01T00:00:00Z'
      };

      service.syncOneProduct('product-2', '/market/path2', true).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request => 
        request.url === `${API_URI.PRODUCT}/sync/product-2` &&
        request.params.get(RequestParam.MARKET_ITEM_PATH) === '/market/path2' &&
        request.params.get(RequestParam.OVERRIDE_MARKET_ITEM_PATH) === 'true'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });

    it('should sync latest releases for products', () => {
      service.syncLatestReleasesForProducts().subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${API_URI.PRODUCT_DETAILS}/sync-release-notes`);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush(null);
    });

    it('should sync github monitor', () => {
      const mockResponse = 'Sync completed';

      service.syncGithubMonitor().subscribe(response => {
        expect(response).toBe(mockResponse);
      });

      const req = httpMock.expectOne(API_URI.SYNC_GITHUB_MONITOR);
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      expect(req.request.responseType).toBe('text');
      req.flush(mockResponse);
    });

    it('should fetch sync task executions', () => {
      const mockExecutions: SyncTaskExecution[] = [
        {
          key: 'syncProducts',
          status: undefined,
          triggeredAt: '2024-01-01T00:00:00Z',
          completedAt: '2024-01-01T00:05:00Z'
        },
        {
          key: 'syncGithubMonitor',
          status: undefined,
          triggeredAt: '2024-01-01T01:00:00Z'
        }
      ];

      service.fetchSyncTaskExecutions().subscribe(executions => {
        expect(executions).toEqual(mockExecutions);
      });

      const req = httpMock.expectOne(API_URI.SYNC_TASK_EXECUTION);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush(mockExecutions);
    });

    it('should sort market extensions with default remainderRule', () => {
      const orderedList = ['product-1', 'product-2', 'product-3'];

      service.sortMarketExtensions(orderedList).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`);
      expect(req.request.method).toBe('POST');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      expect(req.request.body).toEqual({
        orderedListOfProducts: orderedList,
        ruleForRemainder: 'alphabetically'
      });
      req.flush(null);
    });

    it('should sort market extensions with custom remainderRule', () => {
      const orderedList = ['product-1', 'product-2'];
      const remainderRule = 'custom-rule';

      service.sortMarketExtensions(orderedList, remainderRule).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        orderedListOfProducts: orderedList,
        ruleForRemainder: remainderRule
      });
      req.flush(null);
    });
  });

  describe('getSecurityDetails', () => {
    it('should get security details', () => {
      const mockSecurityInfo: ProductSecurityInfo[] = [
        {
          repoName: 'test-repo',
          visibility: 'public',
          archived: false,
          dependabot: { status: 'enabled', alerts: {} },
          codeScanning: { status: 'enabled', alerts: {} },
          secretScanning: { status: 'enabled', numberOfAlerts: 0 },
          branchProtectionEnabled: true,
          lastCommitSHA: 'abc123',
          lastCommitDate: '2024-01-01T00:00:00Z'
        }
      ];

      service.getSecurityDetails().subscribe(details => {
        expect(details).toEqual(mockSecurityInfo);
      });

      const req = httpMock.expectOne(API_URI.SECURITY_MONITOR);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );
      req.flush(mockSecurityInfo);
    });
  });

  describe('getReleaseLettersWithoutPaging', () => {
    it('should get release letters without pagination', () => {
      const mockResponse: ReleaseLetterListApiResponse = {
        _embedded: {
          releaseLetterModelList: [
            {
              sprint: 'S43',
              createdAt: '2024-01-01T00:00:00Z',
              content: 'Release content',
              latest: true
            } as any
          ]
        },
        _links: {
          self: { href: '/api/release-letters' }
        }
      } as any;

      service.getReleaseLettersWithoutPaging().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        API_URI.RELEASE_LETTERS_WITHOUT_PAGINATION
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );

      req.flush(mockResponse);
    });
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

      expect(req.request.context.get(LoadingComponent)).toBe(
        LoadingComponentId.NEWS_PAGE
      );

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

      const req = httpMock.expectOne('/api/release-letters?page=1&size=5');

      expect(req.request.method).toBe('GET');
      expect(req.request.params.keys().length).toBe(0);

      req.flush(mockResponse);
    });

    it('should use custom pageId in HttpContext', () => {
      const criteria: ReleaseLetterCriteria = {} as any;
      const customPageId = 'CUSTOM_PAGE';

      service.getReleaseLetters(criteria, customPageId).subscribe();

      const req = httpMock.expectOne(API_URI.RELEASE_LETTERS);

      expect(req.request.context.get(LoadingComponent)).toBe(customPageId);

      req.flush({} as ReleaseLetterListApiResponse);
    });

    it('should return empty object when request fails', () => {
      const criteria: ReleaseLetterCriteria = {} as any;

      service.getReleaseLetters(criteria).subscribe(response => {
        expect(response).toEqual({} as ReleaseLetterListApiResponse);
      });

      const req = httpMock.expectOne(API_URI.RELEASE_LETTERS);

      req.flush('Error', { status: 500, statusText: 'Server Error' });
    });
  });

  describe('getActiveReleaseLetters', () => {
    it('should get active release letters', () => {
      const mockResponse: ReleaseLetterListApiResponse = {
        _embedded: {
          releaseLetterModelList: [
            {
              sprint: 'S50',
              createdAt: '2024-02-01T00:00:00Z',
              content: 'Active release content'
            } as any
          ]
        },
        _links: {
          self: { href: '/api/active-release-letters' }
        }
      } as any;

      service.getActiveReleaseLetters().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(API_URI.ACTIVE_RELEASE_LETTERS);

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );

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

      const mockResponse: ReleaseLetterApiResponse = {
        sprint: 'S51',
        content: 'New release content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      service.createReleaseLetter(releaseLetterRequest).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(API_URI.RELEASE_LETTERS);

      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(releaseLetterRequest);
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );
      expect(req.request.context.get(ForwardingError)).toBeTrue();

      req.flush(mockResponse);
    });
  });

  describe('updateReleaseLetter', () => {
    it('should update release letter by sprint with ForwardingError context', () => {
      const selectedSprint = 'S51';

      const releaseLetterRequest: ReleaseLetter = {
        sprint: selectedSprint,
        content: 'Updated content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      const mockResponse: ReleaseLetterApiResponse = {
        sprint: selectedSprint,
        content: 'Updated content',
        createdAt: '2024-02-01T00:00:00Z'
      } as any;

      service
        .updateReleaseLetter(selectedSprint, releaseLetterRequest)
        .subscribe(response => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpMock.expectOne(
        `${API_URI.RELEASE_LETTERS}/sprint/${selectedSprint}`
      );

      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(releaseLetterRequest);
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );
      expect(req.request.context.get(ForwardingError)).toBeTrue();

      req.flush(mockResponse);
    });
  });

  describe('getRelaseLetterBySprint', () => {
    it('should get release letter by sprint', () => {
      const sprint = 'S52';

      const mockResponse: ReleaseLetterApiResponse = {
        sprint,
        content: 'Sprint 52 release content',
        createdAt: '2024-02-10T00:00:00Z'
      } as any;

      service.getRelaseLetterBySprint(sprint).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        `${API_URI.RELEASE_LETTERS}/sprint/${sprint}`
      );

      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );

      req.flush(mockResponse);
    });
  });

  describe('deleteReleaseLetterBySprint', () => {
    it('should delete release letter by sprint', () => {
      const sprint = 'S53';

      service.deleteReleaseLetterBySprint(sprint).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(
        `${API_URI.RELEASE_LETTERS}/sprint/${sprint}`
      );

      expect(req.request.method).toBe('DELETE');
      expect(req.request.headers.get('Authorization')).toBe(
        'Bearer test-token'
      );

      req.flush(null);
    });
  });
});
