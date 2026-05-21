import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { AdminDashboardComponent } from './admin-dashboard.component';
import {
  AdminDashboardService,
  SyncTaskExecution
} from './admin-dashboard.service';
import { ProductService } from '../../modules/product/product.service';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { PageTitleService } from '../../shared/services/page-title.service';
import { AdminAuthService } from './admin-auth.service';
import { of, Subject, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { MarketProduct } from '../../shared/models/product.model';
import {
  ERROR_MESSAGES,
  UNAUTHORIZED
} from '../../shared/constants/common.constant';
import { NavigationEnd, provideRouter, Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { SYNC_TASK_KEYS } from '../../shared/constants/admin.constant';

const TEST_CONSTANTS = {
  VALID_PRODUCT_ID: 'portal',
  VALID_MARKET_DIR: 'dir1',
  INVALID_PRODUCT_ID: 'non-existent',
  SUCCESS_MESSAGE: 'Success',
  FAILED_MESSAGE: 'Failed'
};

const createSyncProductValues = (
  component: AdminDashboardComponent,
  productId: string,
  marketDir: string
) => {
  component.productSearch = productId;
  component.marketDirectory = marketDir;
};

const expectSyncValuesToBeReset = (component: AdminDashboardComponent) => {
  expect(component.showSyncProductDialog).toBe(false);
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
  let router: Router;
  let cdr: ChangeDetectorRef;
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let mockAdminService: MockedObject<AdminDashboardService>;
  let mockProductService: MockedObject<ProductService>;
  let mockAuthService: MockedObject<AdminAuthService>;
  let mockTranslateService: MockedObject<TranslateService>;
  let mockPageTitleService: MockedObject<PageTitleService>;

  const mockExecutions: SyncTaskExecution[] = [
    {
      key: SYNC_TASK_KEYS.SYNC_PRODUCTS,
      status: SyncTaskStatus.SUCCESS,
      lastRunDate: '2024-01-01T00:00:00Z',
      completedDate: '2024-01-01T00:05:00Z',
      message: TEST_CONSTANTS.SUCCESS_MESSAGE
    },
    {
      key: SYNC_TASK_KEYS.SYNC_LATEST_RELEASES_FOR_PRODUCTS,
      status: SyncTaskStatus.FAILED,
      lastRunDate: '2024-01-01T00:00:00Z',
      completedDate: '2024-01-01T00:05:00Z',
      message: TEST_CONSTANTS.FAILED_MESSAGE
    }
  ];

  const mockProducts: MarketProduct[] = [
    {
      id: TEST_CONSTANTS.VALID_PRODUCT_ID,
      marketDirectory: TEST_CONSTANTS.VALID_MARKET_DIR
    } as MarketProduct,
    { id: 'demos', marketDirectory: 'dir2' } as MarketProduct,
    { id: 'snowflake-connector', marketDirectory: undefined } as MarketProduct
  ];

  beforeEach(async () => {
    mockAdminService = {
      fetchSyncTaskExecutions: vi
        .fn()
        .mockName('AdminDashboardService.fetchSyncTaskExecutions'),
      syncProducts: vi.fn().mockName('AdminDashboardService.syncProducts'),
      syncLatestReleasesForProducts: vi
        .fn()
        .mockName('AdminDashboardService.syncLatestReleasesForProducts'),
      syncGithubMonitor: vi
        .fn()
        .mockName('AdminDashboardService.syncGithubMonitor'),
      syncOneProduct: vi.fn().mockName('AdminDashboardService.syncOneProduct')
    } as any;
    mockProductService = {
      fetchAllProductsForSync: vi
        .fn()
        .mockName('ProductService.fetchAllProductsForSync')
    } as any;
    mockAuthService = {
      clearToken: vi.fn().mockName('AdminAuthService.clearToken')
    } as any;
    mockTranslateService = {
      instant: vi.fn().mockName('TranslateService.instant'),
      get: vi.fn().mockName('TranslateService.get'),
      use: vi.fn().mockName('TranslateService.use'),
      addLangs: vi.fn().mockName('TranslateService.addLangs'),
      setDefaultLang: vi.fn().mockName('TranslateService.setDefaultLang')
    } as any;
    mockTranslateService.get.mockReturnValue(of('translated'));
    mockTranslateService.instant.mockImplementation((key: string | string[]) => key as string);
    mockPageTitleService = {
      setTitleOnLangChange: vi
        .fn()
        .mockName('PageTitleService.setTitleOnLangChange'),
      setTitle: vi.fn().mockName('PageTitleService.setTitle'),
      ngOnDestroy: vi.fn().mockName('PageTitleService.ngOnDestroy')
    } as any;

    const mockLanguageService = {
      selectedLanguage: vi.fn().mockName('LanguageService.selectedLanguage')
    };
    mockLanguageService.selectedLanguage.mockReturnValue('en');

    const mockThemeService = {
      isDarkMode: vi.fn().mockName('ThemeService.isDarkMode')
    };
    mockThemeService.isDarkMode.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: AdminDashboardService, useValue: mockAdminService },
        { provide: ProductService, useValue: mockProductService },
        { provide: AdminAuthService, useValue: mockAuthService },
        { provide: PageTitleService, useValue: mockPageTitleService },
        { provide: LanguageService, useValue: mockLanguageService },
        { provide: ThemeService, useValue: mockThemeService }
      ]
    }).compileComponents();

    mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
      of(mockExecutions)
    );
    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;

    router = TestBed.inject(Router);
    cdr = component.cdr;
    vi.spyOn(cdr, 'markForCheck');
  });

  describe('ngOnInit', () => {
    it('should load executions on init', () => {
      fixture.detectChanges();
      expect(mockAdminService.fetchSyncTaskExecutions).toHaveBeenCalled();
      expect(mockPageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
        'common.admin.sync.pageTitle'
      );
    });

    it('should handle auth error on init', () => {
      const errorResponse = new HttpErrorResponse({ status: UNAUTHORIZED });
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        throwError(() => errorResponse)
      );

      fixture.detectChanges();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.INVALID_TOKEN);
      expect(mockAuthService.clearToken).toHaveBeenCalled();
    });

    it('should handle non-auth error on init', () => {
      const errorResponse = new HttpErrorResponse({ status: 500 });
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        throwError(() => errorResponse)
      );

      fixture.detectChanges();

      expect(component.errorMessage).toBe(ERROR_MESSAGES.FETCH_FAILURE);
    });
  });

  describe('trigger', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should open sync one product dialog for syncOneProduct task', () => {
      mockProductService.fetchAllProductsForSync.mockReturnValue(
        of(mockProducts)
      );
      const syncTask = component.syncTasks.find(
        t => t.key === SYNC_TASK_KEYS.SYNC_ONE_PRODUCT
      )!;

      component.trigger(syncTask);

      expect(component.showSyncProductDialog).toBe(true);
      expect(component.products).toEqual(mockProducts);
    });

    it('should trigger syncProducts successfully', () => {
      mockAdminService.syncProducts.mockReturnValue(of());
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        of(mockExecutions)
      );
      const syncTask = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;

      component.trigger(syncTask);

      expect(mockAdminService.syncProducts).toHaveBeenCalled();
      expectSyncTaskState(component, SYNC_TASK_KEYS.SYNC_PRODUCTS, SyncTaskStatus.RUNNING);
      expect(syncTask.completedDate).toBeDefined();
      expect(component.loadingSyncTaskKey).toBeNull();
    });

    it('should trigger syncLatestReleasesForProducts successfully', () => {
      mockAdminService.syncLatestReleasesForProducts.mockReturnValue(of());
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        of(mockExecutions)
      );
      const syncTask = component.syncTasks.find(
        t => t.key === SYNC_TASK_KEYS.SYNC_LATEST_RELEASES_FOR_PRODUCTS
      )!;

      component.trigger(syncTask);

      expect(mockAdminService.syncLatestReleasesForProducts).toHaveBeenCalled();
      expectSyncTaskState(
        component,
        SYNC_TASK_KEYS.SYNC_LATEST_RELEASES_FOR_PRODUCTS,
        SyncTaskStatus.RUNNING
      );
    });

    it('should trigger syncGithubMonitor successfully', () => {
      mockAdminService.syncGithubMonitor.mockReturnValue(of());
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        of(mockExecutions)
      );
      const syncTask = component.syncTasks.find(
        t => t.key === SYNC_TASK_KEYS.SYNC_GITHUB_MONITOR
      )!;

      component.trigger(syncTask);

      expect(mockAdminService.syncGithubMonitor).toHaveBeenCalled();
      expectSyncTaskState(
        component,
        SYNC_TASK_KEYS.SYNC_GITHUB_MONITOR,
        SyncTaskStatus.RUNNING
      );
    });
  });

  describe('applySyncTaskExecutions', () => {
    it('should apply executions with all fields present', () => {
      fixture.detectChanges();
      const syncTask = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;

      expectSyncTaskState(
        component,
        SYNC_TASK_KEYS.SYNC_PRODUCTS,
        SyncTaskStatus.SUCCESS,
        TEST_CONSTANTS.SUCCESS_MESSAGE
      );
      expect(syncTask.lastRunDate).toEqual(new Date('2024-01-01T00:00:00Z'));
      expect(syncTask.completedDate).toEqual(new Date('2024-01-01T00:05:00Z'));
    });

    it('should handle executions with null values', () => {
      const executionsWithNulls: SyncTaskExecution[] = [
        {
          key: SYNC_TASK_KEYS.SYNC_PRODUCTS,
          status: SyncTaskStatus.RUNNING,
          lastRunDate: undefined,
          completedDate: undefined,
          message: undefined
        }
      ];
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        of(executionsWithNulls)
      );

      fixture.detectChanges();
      const syncTask = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;

      expect(syncTask.status).toBe(SyncTaskStatus.RUNNING);
      expect(syncTask.lastRunDate).toBeUndefined();
      expect(syncTask.completedDate).toBeUndefined();
      expect(syncTask.message).toBeUndefined();
    });

    it('should skip execution if sync task not found', () => {
      const executionsWithUnknown: SyncTaskExecution[] = [
        {
          key: 'unknownTask' as any,
          status: SyncTaskStatus.SUCCESS,
          lastRunDate: '2024-01-01T00:00:00Z',
          completedDate: '2024-01-01T00:05:00Z',
          message: 'Success'
        }
      ];
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(
        of(executionsWithUnknown)
      );

      expect(() => fixture.detectChanges()).not.toThrow();
    });
  });

  describe('getStatusClass', () => {
    it('should return correct class for SUCCESS status', () => {
      expect(component.getStatusClass(SyncTaskStatus.SUCCESS)).toBe(
        'text-success'
      );
    });

    it('should return correct class for FAILED status', () => {
      expect(component.getStatusClass(SyncTaskStatus.FAILED)).toBe(
        'text-danger'
      );
    });

    it('should return correct class for RUNNING status', () => {
      expect(component.getStatusClass(SyncTaskStatus.RUNNING)).toBe(
        'text-warning'
      );
    });

    it('should return empty string for undefined status', () => {
      expect(component.getStatusClass()).toBe('');
    });
  });

  describe('handleSyncDialogConfirm', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.products = mockProducts;
    });

    it('should do nothing when no selected task', () => {
      component.selectedTask.set(null);

      expect(() =>
        component.handleSyncDialogConfirm({
          productId: 'portal',
          marketDirectory: 'dir1',
          override: false
        })
      ).not.toThrow();
    });

    it('should mark syncOneProduct as failed when invalid input', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_ONE_PRODUCT)!;
      component.selectedTask.set(task);

      component.handleSyncDialogConfirm({
        productId: 'invalid',
        marketDirectory: '',
        override: false
      });

      expectSyncTaskState(
        component,
        SYNC_TASK_KEYS.SYNC_ONE_PRODUCT,
        SyncTaskStatus.FAILED,
        'common.admin.sync.syncProductDialog.validationMessage'
      );
    });

    it('should execute syncOneProduct when valid', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_ONE_PRODUCT)!;
      component.selectedTask.set(task);

      mockAdminService.syncOneProduct.mockReturnValue(of());
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(of(mockExecutions));

      component.handleSyncDialogConfirm({
        productId: 'portal',
        marketDirectory: 'dir1',
        override: true
      });

      expect(mockAdminService.syncOneProduct).toHaveBeenCalledWith('portal', 'dir1', true);

      expectSyncTaskState(component, SYNC_TASK_KEYS.SYNC_ONE_PRODUCT, SyncTaskStatus.RUNNING);
      expect(component.showSyncProductDialog).toBe(false);
    });

    it('should execute syncZipArtifacts when task is not syncOneProduct', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS)!;
      component.selectedTask.set(task);

      mockAdminService.syncZipArtifacts = vi.fn().mockReturnValue(of());
      mockAdminService.fetchSyncTaskExecutions.mockReturnValue(of(mockExecutions));

      component.handleSyncDialogConfirm({
        productId: 'portal',
        marketDirectory: '',
        override: false
      });

      expect(mockAdminService.syncZipArtifacts).toHaveBeenCalledWith(false, 'portal');

      expectSyncTaskState(component, SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS, SyncTaskStatus.RUNNING);
    });
  });

  describe('executeTask', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set task to running and reset dialog', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      const request$ = of();

      component.showSyncProductDialog = true;

      (component as any).executeTask(task, request$);

      expect(task.status).toBe(SyncTaskStatus.RUNNING);
      expect(component.showSyncProductDialog).toBe(false);
    });

    it('should handle success correctly', async () => {
    const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;

    mockAdminService.fetchSyncTaskExecutions.mockReturnValue(of(mockExecutions));

    (component as any).executeTask(task, of(null));

    await new Promise(resolve => setTimeout(resolve, 0));

    expect(task.status).toBe(SyncTaskStatus.SUCCESS);
    });

    it('should handle failure correctly', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;

      (component as any).executeTask(
        task,
        throwError(() => new Error('fail'))
      );

      expect(task.status).toBe(SyncTaskStatus.FAILED);
    });
  });

  describe('openSyncProductDialog', () => {
    it('should load products and open dialog', async () => {
      mockProductService.fetchAllProductsForSync.mockReturnValue(of(mockProducts));

      await (component as any).openSyncProductDialog();

      expect(component.products).toEqual(mockProducts);
      expect(component.showSyncProductDialog).toBe(true);
    });
  });

  describe('Product dropdown', () => {
    beforeEach(() => {
      fixture.detectChanges();
      const manyProducts = Array.from(
        { length: 20 },
        (_, i) =>
          ({
            id: `product-${i}`,
            marketDirectory: `dir${i}`
          }) as MarketProduct
      );
      component.products = manyProducts;
    });

    it('should open dropdown with first 10 products', () => {
      component.openDropdown();

      expect(component.dropdownOpen).toBe(true);
    });

    it('should select product and close dropdown', () => {
      const product = {
        id: 'product-5',
        marketDirectory: 'dir5'
      } as MarketProduct;

      component.selectProduct(product);

      expect(component.productSearch).toBe('product-5');
      expect(component.marketDirectory).toBe('dir5');
      expect(component.dropdownOpen).toBe(false);
    });

    it('should handle product with null market directory', () => {
      const product = {
        id: 'snowflake-connector',
        marketDirectory: undefined
      } as MarketProduct;

      component.selectProduct(product);

      expect(component.productSearch).toBe('snowflake-connector');
      expect(component.marketDirectory).toBe('');
      expect(component.dropdownOpen).toBe(false);
    });
  });

  it('should set showSyncTask to true on route deactivate', async () => {
    component.showSyncTask = false;

    component.onRouteDeactivate();

    expect(component.showSyncTask).toBe(false);

    await Promise.resolve();

    expect(component.showSyncTask).toBe(true);
    expect(cdr.markForCheck).toHaveBeenCalled();
  });

  it('should set page title when navigating to /internal-dashboard', () => {
    const events$ = new Subject<any>();

    vi.spyOn(router, 'events', 'get').mockReturnValue(events$.asObservable());

    component.ngOnInit();

    events$.next(
      new NavigationEnd(1, '/internal-dashboard', '/internal-dashboard')
    );

    expect(mockPageTitleService.setTitleOnLangChange).toHaveBeenCalledWith(
      'common.admin.sync.pageTitle'
    );
  });

  describe('openLog', () => {
    let modalServiceSpy: any;
    let logStreamSpy: any;
    let mockModalRef: any;

    beforeEach(() => {
      mockModalRef = {
        shown: {
          subscribe: vi.fn().mockImplementation((cb: () => void) => cb())
        },
        dismissed: { subscribe: vi.fn() },
        close: vi.fn(),
        dismiss: vi.fn()
      };

      modalServiceSpy = TestBed.inject(NgbModal) as any;
      vi.spyOn(modalServiceSpy, 'open').mockReturnValue(mockModalRef);

      logStreamSpy = component.logStream as any;
      vi.spyOn(logStreamSpy, 'hasLogs').mockReturnValue(false);
      vi.spyOn(logStreamSpy, 'connectTask');

      fixture.detectChanges();
    });

    it('should set selectedTask to the given syncTask', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      component.openLog(task);
      expect(component.selectedTask()).toEqual(task);
    });

    it('should open modal with size xl when task is RUNNING', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.RUNNING;

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ size: 'xl' })
      );
    });

    it('should open modal with size xl when task has logs', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.SUCCESS;
      logStreamSpy.hasLogs.mockReturnValue(true);

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ size: 'xl' })
      );
    });

    it('should open modal with size md when task is not running and has no logs', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.SUCCESS;
      logStreamSpy.hasLogs.mockReturnValue(false);

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ size: 'md' })
      );
    });

    it('should set windowClass has-logs when task is RUNNING', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.RUNNING;

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ windowClass: 'log-modal has-logs' })
      );
    });

    it('should set windowClass no-logs when task has no logs and is not running', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.SUCCESS;

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ windowClass: 'log-modal no-logs' })
      );
    });

    it('should set modalDialogClass has-logs when has logs', () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      logStreamSpy.hasLogs.mockReturnValue(true);

      component.openLog(task);

      expect(modalServiceSpy.open).toHaveBeenCalledWith(
        expect.anything(),
        expect.objectContaining({ modalDialogClass: 'log-modal has-logs' })
      );
    });

    it('should call logStream.connectTask when task is RUNNING and modal shown', async () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.RUNNING;

      component.openLog(task);

      await Promise.resolve(); // thay tick()

      expect(logStreamSpy.connectTask).toHaveBeenCalledWith(SYNC_TASK_KEYS.SYNC_PRODUCTS);
    });

    it('should NOT call logStream.connectTask when task is not RUNNING', async () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      task.status = SyncTaskStatus.SUCCESS;

      component.openLog(task);

      await Promise.resolve();

      expect(logStreamSpy.connectTask).not.toHaveBeenCalled();
    });

    it('should dispatch resize event after modal shown', async () => {
      const task = component.syncTasks.find(t => t.key === SYNC_TASK_KEYS.SYNC_PRODUCTS)!;
      const dispatchSpy = vi.spyOn(globalThis, 'dispatchEvent');
      vi.useFakeTimers();
      component.openLog(task);
      await Promise.resolve();
      vi.runAllTimers();
      expect(dispatchSpy).toHaveBeenCalledWith(expect.any(Event));
      vi.useRealTimers();
    });
  });

  describe('toggleExpand', () => {
    beforeEach(() => fixture.detectChanges());

    it('should add index when not expanded', () => {
      component.toggleExpand(0);
      expect(component.expandedLogs().has(0)).toBe(true);
    });

    it('should remove index when already expanded', () => {
      component.toggleExpand(1);
      component.toggleExpand(1);
      expect(component.expandedLogs().has(1)).toBe(false);
    });

    it('should toggle multiple indexes independently', () => {
      component.toggleExpand(0);
      component.toggleExpand(2);

      expect(component.expandedLogs().has(0)).toBe(true);
      expect(component.expandedLogs().has(2)).toBe(true);
      expect(component.expandedLogs().has(1)).toBe(false);
    });

    it('should not affect other indexes when collapsing one', () => {
      component.toggleExpand(0);
      component.toggleExpand(1);
      component.toggleExpand(0);

      expect(component.expandedLogs().has(0)).toBe(false);
      expect(component.expandedLogs().has(1)).toBe(true);
    });

    it('should create new Set reference on each toggle', () => {
      const before = component.expandedLogs();
      component.toggleExpand(0);
      expect(component.expandedLogs()).not.toBe(before);
    });
  });

  describe('isExpanded', () => {
    beforeEach(() => fixture.detectChanges());

    it('should return false when index not expanded', () => {
      expect(component.isExpanded(0)).toBe(false);
    });

    it('should return true after toggleExpand', () => {
      component.toggleExpand(3);
      expect(component.isExpanded(3)).toBe(true);
    });

    it('should return false after toggling twice', () => {
      component.toggleExpand(5);
      component.toggleExpand(5);
      expect(component.isExpanded(5)).toBe(false);
    });

    it('should be consistent with expandedLogs signal', () => {
      component.toggleExpand(2);
      expect(component.isExpanded(2)).toBe(component.expandedLogs().has(2));
    });

    it('should not set page title for other routes', () => {
      const events$ = new Subject<any>();
      vi.spyOn(router, 'events', 'get').mockReturnValue(events$.asObservable());
      component.ngOnInit();
      mockPageTitleService.setTitleOnLangChange.mockClear();
      events$.next(new NavigationEnd(1, '/other', '/other'));
      expect(mockPageTitleService.setTitleOnLangChange).not.toHaveBeenCalled();
    });
  });
});
