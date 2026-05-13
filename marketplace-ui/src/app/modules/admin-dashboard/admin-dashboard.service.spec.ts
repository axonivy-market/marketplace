import { afterEach, beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
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
import { LoadingComponent } from '../../core/interceptors/api.interceptor';
import { ReleaseLetter } from '../../shared/models/release-letter-request.model';
import { ReleaseLetterApiResponse } from '../../shared/models/apis/release-letter-response.model';
import { AUTHORIZATION_HEADER } from '../../shared/constants/common.constant';
import { SYNC_TASKS, SYNC_TASK_KEYS } from '../../shared/constants/admin.constant';

describe('AdminDashboardService', () => {
  let service: AdminDashboardService;
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
        key: SYNC_TASK_KEYS.SYNC_PRODUCTS,
        status: undefined,
        lastRunDate: '2024-01-01T00:00:00Z'
      };

      service.syncProducts(true).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        request =>
          request.url === `${API_URI.PRODUCT}/sync` &&
          request.params.get(RequestParam.RESET_SYNC) === 'true'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });

    it('should sync one product with overrideMarketItemPath=true', () => {
      const mockResponse: SyncTaskExecution = {
        key: SYNC_TASK_KEYS.SYNC_ONE_PRODUCT,
        status: undefined,
        lastRunDate: '2024-01-01T00:00:00Z'
      };

      service
        .syncOneProduct('product-2', '/market/path2', true)
        .subscribe(response => {
          expect(response).toEqual(mockResponse);
        });

      const req = httpMock.expectOne(
        request =>
          request.url === `${API_URI.PRODUCT}/sync/product-2` &&
          request.params.get(RequestParam.MARKET_ITEM_PATH) ===
            '/market/path2' &&
          request.params.get(RequestParam.OVERRIDE_MARKET_ITEM_PATH) === 'true'
      );
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });

    it('should sync latest releases for products', () => {
      service.syncLatestReleasesForProducts().subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(
        `${API_URI.PRODUCT_DETAILS}/sync-release-notes`
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe(
        'Bearer test-token'
      );
      req.flush(null);
    });

    it('should sync github monitor', () => {
      const mockResponse = 'Sync completed';

      service.syncGithubMonitor().subscribe(response => {
        expect(response).toBe(mockResponse);
      });

      const req = httpMock.expectOne(API_URI.SYNC_GITHUB_MONITOR);
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe(
        'Bearer test-token'
      );
      expect(req.request.responseType).toBe('text');
      req.flush(mockResponse);
    });

    it('should fetch sync task executions', () => {
      const mockExecutions: SyncTaskExecution[] = [
        {
          key: SYNC_TASK_KEYS.SYNC_PRODUCTS,
          status: undefined,
          lastRunDate: '2024-01-01T00:00:00Z',
          completedDate: '2024-01-01T00:05:00Z'
        },
        {
          key: SYNC_TASK_KEYS.SYNC_GITHUB_MONITOR,
          status: undefined,
          lastRunDate: '2024-01-01T01:00:00Z'
        }
      ];

      service.fetchSyncTaskExecutions().subscribe(executions => {
        expect(executions).toEqual(mockExecutions);
      });

      const req = httpMock.expectOne(API_URI.SYNC_TASK_EXECUTION);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe(
        'Bearer test-token'
      );
      req.flush(mockExecutions);
    });

    it('should sort market extensions with default remainderRule', () => {
      const orderedList = ['product-1', 'product-2', 'product-3'];

      service.sortMarketExtensions(orderedList).subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(
        `${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe(
        'Bearer test-token'
      );
      expect(req.request.body).toEqual({
        orderedListOfProducts: orderedList,
        ruleForRemainder: 'alphabetically'
      });
      req.flush(null);
    });

    it('should sort market extensions with custom remainderRule', () => {
      const orderedList = ['product-1', 'product-2'];
      const remainderRule = 'custom-rule';

      service
        .sortMarketExtensions(orderedList, remainderRule)
        .subscribe(response => {
          expect(response).toBeNull();
        });

      const req = httpMock.expectOne(
        `${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`
      );
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        orderedListOfProducts: orderedList,
        ruleForRemainder: remainderRule
      });
      req.flush(null);
    });

    it('should fetch custom sort configuration', () => {
      const mockConfig = {
        orderedListOfProducts: ['a', 'b'],
        ruleForRemainder: 'alphabetically'
      };

      service.getCustomSort().subscribe(config => {
        expect(config).toEqual(mockConfig);
      });

      const req = httpMock.expectOne(
        `${API_URI.PRODUCT_MARKETPLACE_DATA}/custom-sort`
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockConfig);
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
          secretScanning: { status: 'enabled', numberOfSecretScanningAlerts: 0 },
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
      expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe(
        'Bearer test-token'
      );
      req.flush(mockSecurityInfo);
    });
  });

  it('should sync zip artifacts for all products when productId is omitted', () => {
    const mockResponse: SyncTaskExecution = {
      key: SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS,
      status: undefined,
      completedDate: '2024-01-01T00:00:00Z'
    };

    // call with resetSync = true and no productId
    service.syncZipArtifacts(true).subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      request =>
        request.url === `${API_URI.PRODUCT}/zip-sync` &&
        request.params.get(RequestParam.RESET_SYNC) === 'true' &&
        !request.params.has(RequestParam.ID)
    );
    expect(req.request.method).toBe('PUT');
    // body should be an empty object as per implementation
    expect(req.request.body).toEqual({});
    expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');
    req.flush(mockResponse);
  });

  it('should sync zip artifacts for a specific product when productId is provided', () => {
    const mockResponse: SyncTaskExecution = {
      key: SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS,
      status: undefined,
      completedDate: '2024-01-01T00:00:00Z'
    };

    // call with resetSync = false (default) and a specific productId
    service.syncZipArtifacts(false, 'product-1').subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      request =>
        request.url === `${API_URI.PRODUCT}/zip-sync` &&
        request.params.get(RequestParam.RESET_SYNC) === 'false' &&
        request.params.get(RequestParam.ID) === 'product-1'
    );
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({});
    expect(req.request.headers.get(AUTHORIZATION_HEADER)).toBe('Bearer test-token');
    req.flush(mockResponse);
  });
});
