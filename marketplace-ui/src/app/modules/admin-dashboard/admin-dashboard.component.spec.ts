import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminDashboardService, SyncTaskExecution } from './admin-dashboard.service';
import { ProductService } from '../../modules/product/product.service';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PageTitleService } from '../../shared/services/page-title.service';
import { AdminAuthService } from './admin-auth.service';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { MarketProduct } from '../../shared/models/product.model';
import { ERROR_MESSAGES, UNAUTHORIZED } from '../../shared/constants/common.constant';

const TEST_CONSTANTS = {
  VALID_PRODUCT_ID: 'portal',
  VALID_MARKET_DIR: 'dir1',
  INVALID_PRODUCT_ID: 'non-existent',
  SUCCESS_MESSAGE: 'Success',
  FAILED_MESSAGE: 'Failed'
};

const createSyncProductValues = (component: AdminDashboardComponent, productId: string, marketDir: string) => {
  component.productSearch = productId;
  component.marketDirectory = marketDir;
};

const expectSyncValuesToBeReset = (component: AdminDashboardComponent) => {
  expect(component.showSyncOneProductDialog).toBe(false);
  expect(component.productSearch).toBe('');
  expect(component.marketDirectory).toBe('');
  expect(component.overrideMarketItemPath).toBe(false);
};

const expectSyncTaskState = (
  component: AdminDashboardComponent,
  taskKey: string,
  expectedStatus: SyncTaskStatus,
  expectedMessage?: string
) => {
  const syncTask = component.syncTasks.find(t => t.key === taskKey);
  expect(syncTask?.status).toBe(expectedStatus);
  if (expectedMessage !== undefined) {
    expect(syncTask?.message).toBe(expectedMessage);
  }
};

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let mockAdminService: jasmine.SpyObj<AdminDashboardService>;
  let mockProductService: jasmine.SpyObj<ProductService>;
  let mockAuthService: jasmine.SpyObj<AdminAuthService>;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;
  let mockPageTitleService: jasmine.SpyObj<PageTitleService>;

  const mockExecutions: SyncTaskExecution[] = [
    {
      key: 'syncProducts',
      status: SyncTaskStatus.SUCCESS,
      triggeredAt: '2024-01-01T00:00:00Z',
      completedAt: '2024-01-01T00:05:00Z',
      message: TEST_CONSTANTS.SUCCESS_MESSAGE
    },
    {
      key: 'syncLatestReleasesForProducts',
      status: SyncTaskStatus.FAILED,
      triggeredAt: '2024-01-01T00:00:00Z',
      completedAt: '2024-01-01T00:05:00Z',
      message: TEST_CONSTANTS.FAILED_MESSAGE
    }
  ];

  const mockProducts: MarketProduct[] = [
    { id: TEST_CONSTANTS.VALID_PRODUCT_ID, marketDirectory: TEST_CONSTANTS.VALID_MARKET_DIR } as MarketProduct,
    { id: 'demos', marketDirectory: 'dir2' } as MarketProduct,
    { id: 'snowflake-connector', marketDirectory: undefined } as MarketProduct
  ];

  beforeEach(async () => {
    mockAdminService = jasmine.createSpyObj('AdminDashboardService', [
      'fetchSyncTaskExecutions',
      'syncProducts',
      'syncLatestReleasesForProducts',
      'syncGithubMonitor',
      'syncOneProduct'
    ]);
    mockProductService = jasmine.createSpyObj('ProductService', ['fetchAllProductsForSync']);
    mockAuthService = jasmine.createSpyObj('AdminAuthService', ['clearToken']);
    mockTranslateService = jasmine.createSpyObj('TranslateService', ['instant', 'get', 'use', 'addLangs', 'setDefaultLang']);
    mockTranslateService.get.and.returnValue(of('translated'));
    mockTranslateService.instant.and.callFake((key: string) => key);
    mockPageTitleService = jasmine.createSpyObj('PageTitleService', ['setTitleOnLangChange']);

    const mockLanguageService = jasmine.createSpyObj('LanguageService', ['selectedLanguage']);
    mockLanguageService.selectedLanguage.and.returnValue('en');

    const mockThemeService = jasmine.createSpyObj('ThemeService', ['isDarkMode']);
    mockThemeService.isDarkMode.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AdminDashboardService, useValue: mockAdminService },
        { provide: ProductService, useValue: mockProductService },
        { provide: AdminAuthService, useValue: mockAuthService },
        { provide: PageTitleService, useValue: mockPageTitleService },
        { provide: LanguageService, useValue: mockLanguageService },
        { provide: ThemeService, useValue: mockThemeService }
      ]
    }).compileComponents();

    mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should load executions on init', () => {
      fixture.detectChanges();
      expect(mockAdminService.fetchSyncTaskExecutions).toHaveBeenCalled();
      expect(mockPageTitleService.setTitleOnLangChange).toHaveBeenCalledWith('common.admin.sync.pageTitle');
    });

    it('should handle auth error on init', () => {
      const errorResponse = new HttpErrorResponse({ status: UNAUTHORIZED });
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(throwError(() => errorResponse));

      fixture.detectChanges();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.INVALID_TOKEN);
      expect(mockAuthService.clearToken).toHaveBeenCalled();
    });

    it('should handle non-auth error on init', () => {
      const errorResponse = new HttpErrorResponse({ status: 500 });
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(throwError(() => errorResponse));

      fixture.detectChanges();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.FETCH_FAILURE);
      expect(mockAuthService.clearToken).toHaveBeenCalled();
    });
  });

  describe('trigger', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open sync one product dialog for syncOneProduct task', async () => {
      mockProductService.fetchAllProductsForSync.and.returnValue(Promise.resolve(mockProducts));
      const syncTask = component.syncTasks.find(t => t.key === 'syncOneProduct')!;

      await component.trigger(syncTask);

      expect(component.showSyncOneProductDialog).toBe(true);
      expect(component.products).toEqual(mockProducts);
      expect(component.filteredProducts.length).toBe(3);
    });

    it('should trigger syncProducts successfully', fakeAsync(() => {
      mockAdminService.syncProducts.and.returnValue(of());
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      const syncTask = component.syncTasks.find(t => t.key === 'syncProducts')!;

      component.trigger(syncTask);
      tick();

      expect(mockAdminService.syncProducts).toHaveBeenCalled();
      expectSyncTaskState(component, 'syncProducts', SyncTaskStatus.RUNNING);
      expect(syncTask.completedAt).toBeDefined();
      expect(component.loadingSyncTaskKey).toBeNull();
    }));

    it('should trigger syncLatestReleasesForProducts successfully', fakeAsync(() => {
      mockAdminService.syncLatestReleasesForProducts.and.returnValue(of());
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      const syncTask = component.syncTasks.find(t => t.key === 'syncLatestReleasesForProducts')!;

      component.trigger(syncTask);
      tick();

      expect(mockAdminService.syncLatestReleasesForProducts).toHaveBeenCalled();
      expectSyncTaskState(component, 'syncLatestReleasesForProducts', SyncTaskStatus.RUNNING);
    }));

    it('should trigger syncGithubMonitor successfully', fakeAsync(() => {
      mockAdminService.syncGithubMonitor.and.returnValue(of());
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      const syncTask = component.syncTasks.find(t => t.key === 'syncGithubMonitor')!;

      component.trigger(syncTask);
      tick();

      expect(mockAdminService.syncGithubMonitor).toHaveBeenCalled();
      expectSyncTaskState(component, 'syncGithubMonitor', SyncTaskStatus.RUNNING);
    }));

    it('should not reload executions on failure when not authenticated', fakeAsync(() => {
      mockAdminService.syncProducts.and.returnValue(throwError(() => new Error('Sync failed')));
      const syncTask = component.syncTasks.find(t => t.key === 'syncProducts')!;
      component.isAuthenticated = false;
      mockAdminService.fetchSyncTaskExecutions.calls.reset();

      component.trigger(syncTask);
      tick();

      expectSyncTaskState(component, 'syncProducts', SyncTaskStatus.FAILED);
      expect(mockAdminService.fetchSyncTaskExecutions).not.toHaveBeenCalled();
    }));
  });

  describe('applySyncTaskExecutions', () => {
    it('should apply executions with all fields present', () => {
      fixture.detectChanges();
      const syncTask = component.syncTasks.find(t => t.key === 'syncProducts')!;

      expectSyncTaskState(component, 'syncProducts', SyncTaskStatus.SUCCESS, TEST_CONSTANTS.SUCCESS_MESSAGE);
      expect(syncTask.triggeredAt).toEqual(new Date('2024-01-01T00:00:00Z'));
      expect(syncTask.completedAt).toEqual(new Date('2024-01-01T00:05:00Z'));
    });

    it('should handle executions with null values', () => {
      const executionsWithNulls: SyncTaskExecution[] = [
        {
          key: 'syncProducts',
          status: SyncTaskStatus.RUNNING,
          triggeredAt: undefined,
          completedAt: undefined,
          message: undefined
        }
      ];
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(executionsWithNulls));
      
      fixture.detectChanges();
      const syncTask = component.syncTasks.find(t => t.key === 'syncProducts')!;

      expect(syncTask.status).toBe(SyncTaskStatus.RUNNING);
      expect(syncTask.triggeredAt).toBeUndefined();
      expect(syncTask.completedAt).toBeUndefined();
      expect(syncTask.message).toBeUndefined();
    });

    it('should skip execution if sync task not found', () => {
      const executionsWithUnknown: SyncTaskExecution[] = [
        {
          key: 'unknownTask' as any,
          status: SyncTaskStatus.SUCCESS,
          triggeredAt: '2024-01-01T00:00:00Z',
          completedAt: '2024-01-01T00:05:00Z',
          message: 'Success'
        }
      ];
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(executionsWithUnknown));
      
      expect(() => fixture.detectChanges()).not.toThrow();
    });
  });

  describe('getStatusClass', () => {
    it('should return correct class for SUCCESS status', () => {
      expect(component.getStatusClass(SyncTaskStatus.SUCCESS)).toBe('text-success');
    });

    it('should return correct class for FAILED status', () => {
      expect(component.getStatusClass(SyncTaskStatus.FAILED)).toBe('text-danger');
    });

    it('should return correct class for RUNNING status', () => {
      expect(component.getStatusClass(SyncTaskStatus.RUNNING)).toBe('text-warning');
    });

    it('should return empty string for undefined status', () => {
      expect(component.getStatusClass(undefined)).toBe('');
    });
  });

  describe('confirmSyncOneProduct', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.products = mockProducts;
    });

    it('should execute sync when values are valid', fakeAsync(() => {
      mockAdminService.syncOneProduct.and.returnValue(of());
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      createSyncProductValues(component, TEST_CONSTANTS.VALID_PRODUCT_ID, TEST_CONSTANTS.VALID_MARKET_DIR);
      component.overrideMarketItemPath = true;

      component.confirmSyncOneProduct();
      tick();

      expect(mockAdminService.syncOneProduct).toHaveBeenCalledWith(TEST_CONSTANTS.VALID_PRODUCT_ID, TEST_CONSTANTS.VALID_MARKET_DIR, true);
      expect(component.showSyncOneProductDialog).toBe(false);
      expectSyncTaskState(component, 'syncOneProduct', SyncTaskStatus.RUNNING);
    }));

    it('should trim market directory before syncing', fakeAsync(() => {
      mockAdminService.syncOneProduct.and.returnValue(of());
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      createSyncProductValues(component, TEST_CONSTANTS.VALID_PRODUCT_ID, '  dir1  ');

      component.confirmSyncOneProduct();
      tick();

      expect(mockAdminService.syncOneProduct).toHaveBeenCalledWith('portal', 'dir1', false);
    }));

    it('should mark as failed when product not found', () => {
      component.productSearch = 'non-existent';
      component.marketDirectory = 'dir1';

      component.confirmSyncOneProduct();

      expectSyncTaskState(component, 'syncOneProduct', SyncTaskStatus.FAILED, 'common.admin.sync.syncOneProductDialog.validationMessage');
      expect(mockAdminService.syncOneProduct).not.toHaveBeenCalled();
    });

    it('should mark as failed when market directory is empty', () => {
      component.productSearch = 'portal';
      component.marketDirectory = '';

      component.confirmSyncOneProduct();

      expectSyncTaskState(component, 'syncOneProduct', SyncTaskStatus.FAILED);
      expect(mockAdminService.syncOneProduct).not.toHaveBeenCalled();
    });

    it('should do nothing when sync task not found', () => {
      component.syncTasks = [];
      component.productSearch = 'portal';
      component.marketDirectory = 'dir1';

      expect(() => component.confirmSyncOneProduct()).not.toThrow();
    });

    it('should handle sync one product failure', fakeAsync(() => {
      mockAdminService.syncOneProduct.and.returnValue(throwError(() => new Error('Sync failed')));
      mockAdminService.fetchSyncTaskExecutions.and.returnValue(of(mockExecutions));
      component.productSearch = 'portal';
      component.marketDirectory = 'dir1';
      component.isAuthenticated = true;

      component.confirmSyncOneProduct();
      tick();

      expectSyncTaskState(component, 'syncOneProduct', SyncTaskStatus.FAILED);
    }));
  });

  describe('isValidSyncOneProductValues', () => {
    beforeEach(() => {
      component.products = mockProducts;
    });

    it('should return true when product exists and market directory is set', () => {
      createSyncProductValues(component, TEST_CONSTANTS.VALID_PRODUCT_ID, TEST_CONSTANTS.VALID_MARKET_DIR);

      expect(component.isValidSyncOneProductValues()).toBe(true);
    });

    it('should return false when product does not exist', () => {
      createSyncProductValues(component, TEST_CONSTANTS.INVALID_PRODUCT_ID, TEST_CONSTANTS.VALID_MARKET_DIR);

      expect(component.isValidSyncOneProductValues()).toBe(false);
    });

    it('should return false when market directory is empty', () => {
      createSyncProductValues(component, TEST_CONSTANTS.VALID_PRODUCT_ID, '');

      expect(component.isValidSyncOneProductValues()).toBe(false);
    });

    it('should reset all sync one product values', () => {
      component.showSyncOneProductDialog = true;
      createSyncProductValues(component, TEST_CONSTANTS.VALID_PRODUCT_ID, TEST_CONSTANTS.VALID_MARKET_DIR);
      component.overrideMarketItemPath = true;

      component.cancelSyncOneProduct();

      expectSyncValuesToBeReset(component);
    });
  });

  describe('Product dropdown', () => {
    beforeEach(() => {
      fixture.detectChanges();
      const manyProducts = Array.from({ length: 20 }, (_, i) => ({
        id: `product-${i}`,
        marketDirectory: `dir${i}`
      } as MarketProduct));
      component.products = manyProducts;
    });

    it('should open dropdown with first 10 products', () => {
      component.openDropdown();

      expect(component.dropdownOpen).toBe(true);
      expect(component.filteredProducts.length).toBe(10);
    });

    it('should filter products by search term', () => {
      component.productSearch = 'product-1';

      component.filterProducts();

      expect(component.dropdownOpen).toBe(true);
      expect(component.filteredProducts.length).toBeGreaterThan(0);
      expect(component.filteredProducts.every(p => p.id.includes('product-1'))).toBe(true);
    });

    it('should limit filtered products to 10', () => {
      component.productSearch = 'product';

      component.filterProducts();

      expect(component.filteredProducts.length).toBe(10);
    });

    it('should clear market directory when search does not match any product', () => {
      component.productSearch = 'non-existent';
      component.marketDirectory = 'some-dir';

      component.filterProducts();

      expect(component.marketDirectory).toBe('');
    });

    it('should keep market directory when search matches a product', () => {
      component.productSearch = 'product-1';
      component.marketDirectory = 'dir1';

      component.filterProducts();

      expect(component.marketDirectory).toBe('dir1');
    });

    it('should select product and close dropdown', () => {
      const product = { id: 'product-5', marketDirectory: 'dir5' } as MarketProduct;

      component.selectProduct(product);

      expect(component.productSearch).toBe('product-5');
      expect(component.marketDirectory).toBe('dir5');
      expect(component.dropdownOpen).toBe(false);
    });

    it('should handle product with null market directory', () => {
      const product = { id: 'snowflake-connector', marketDirectory: undefined } as MarketProduct;

      component.selectProduct(product);

      expect(component.productSearch).toBe('snowflake-connector');
      expect(component.marketDirectory).toBe('');
      expect(component.dropdownOpen).toBe(false);
    });
  });
});