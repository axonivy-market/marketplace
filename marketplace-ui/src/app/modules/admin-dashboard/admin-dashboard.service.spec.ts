import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminDashboardService, SyncTaskExecution } from './admin-dashboard.service';
import { AdminAuthService } from './admin-auth.service';
import { API_URI } from '../../shared/constants/api.constant';
import { RequestParam } from '../../shared/enums/request-param';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';
import { HttpHeaders } from '@angular/common/http';

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
    it('should sync products with default resetSync=false', () => {
      const mockResponse: SyncTaskExecution = {
        key: 'syncProducts',
        status: undefined,
        triggeredAt: '2024-01-01T00:00:00Z'
      };

      service.syncProducts().subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request => 
        request.url === `${API_URI.PRODUCT}/sync` && 
        request.params.get(RequestParam.RESET_SYNC) === 'false'
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush(mockResponse);
    });

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
  });

  describe('syncOneProduct', () => {
    it('should sync one product with default overrideMarketItemPath=false', () => {
      const mockResponse: SyncTaskExecution = {
        key: 'syncOneProduct',
        status: undefined,
        triggeredAt: '2024-01-01T00:00:00Z'
      };

      service.syncOneProduct('product-1', '/market/path').subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(request => 
        request.url === `${API_URI.PRODUCT}/sync/product-1` &&
        request.params.get(RequestParam.MARKET_ITEM_PATH) === '/market/path' &&
        request.params.get(RequestParam.OVERRIDE_MARKET_ITEM_PATH) === 'false'
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
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
  });

  describe('syncLatestReleasesForProducts', () => {
    it('should sync latest releases for products', () => {
      service.syncLatestReleasesForProducts().subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${API_URI.PRODUCT_DETAILS}/sync-release-notes`);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush(null);
    });
  });

  describe('syncGithubMonitor', () => {
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
  });

  describe('fetchSyncTaskExecutions', () => {
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
  });

  describe('sortMarketExtensions', () => {
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
      expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
      req.flush(mockSecurityInfo);
    });
  });
});
