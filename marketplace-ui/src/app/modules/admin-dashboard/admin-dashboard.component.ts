import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
  ViewEncapsulation,
  TemplateRef,
  ViewChild,
  signal
} from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterModule
} from '@angular/router';
import { EMPTY, filter, finalize, map, Observable, tap } from 'rxjs';
import {
  AdminDashboardService,
  SyncTaskExecution,
  SyncTaskKey
} from './admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import {
  ERROR_MESSAGES,
  UNAUTHORIZED,
  SYNC_TASKS
} from '../../shared/constants/common.constant';
import { HttpErrorResponse } from '@angular/common/http';
import { ThemeService } from '../../core/services/theme/theme.service';
import { PageTitleService } from '../../shared/services/page-title.service';
import { ProductService } from '../../modules/product/product.service';
import { SyncTaskRow } from '../../shared/models/sync-task-execution.model';
import { MarketProduct } from '../../shared/models/product.model';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { AdminAuthService } from './admin-auth.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { LogStreamService } from '../../core/services/logging/log-stream.service';
import { LogParserService, ParsedLog } from './logs-viewer/logs-viewer.service';

const SYNC_ONE_PRODUCT_KEY = 'syncOneProduct';
@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminDashboardComponent implements OnInit {
  service = inject(AdminDashboardService);
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  authService = inject(AdminAuthService);
  cdr = inject(ChangeDetectorRef);
  router = inject(Router);
  activatedRoute = inject(ActivatedRoute);
  modalService = inject(NgbModal);
  logStream = inject(LogStreamService);
  logViewer = inject(LogParserService);
  readonly parsedLogs = signal<ParsedLog[]>([]);
  selectedTask = signal<SyncTaskRow | null>(null);
  logs = this.logStream.getLogsSignal(() => this.selectedTask()?.key);
  readonly expandedLogs = signal<Set<number>>(new Set());
  @ViewChild('changelogContent') changelogContent!: TemplateRef<unknown>;

  isAuthenticated = false;
  errorMessage = '';

  showSyncTask = true;
  loadingSyncTaskKey: SyncTaskKey | null = null;
  showSyncOneProductDialog = false;

  syncTasks = SYNC_TASKS;
  products: MarketProduct[] = [];
  filteredProducts: MarketProduct[] = [];

  productSearch = '';
  marketDirectory = '';
  overrideMarketItemPath = false;
  dropdownOpen = false;

  onRouteActivate(): void {
    queueMicrotask(() => {
      this.showSyncTask = false;
      this.cdr.markForCheck();
    });
  }

  onRouteDeactivate(): void {
    queueMicrotask(() => {
      this.showSyncTask = true;
      this.cdr.markForCheck();
    });
  }

  private readonly syncTaskTriggers: Record<
    SyncTaskKey,
    () => Observable<unknown>
  > = {
    syncProducts: () => this.service.syncProducts(),
    syncLatestReleasesForProducts: () =>
      this.service.syncLatestReleasesForProducts(),
    syncGithubMonitor: () => this.service.syncGithubMonitor(),
    syncOneProduct: () => EMPTY
  };

  ngOnInit(): void {
    this.loadExecutions();
    this.pageTitleService.setTitleOnLangChange('common.admin.sync.pageTitle');
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        if (event.urlAfterRedirects === '/internal-dashboard') {
          this.pageTitleService.setTitleOnLangChange(
            'common.admin.sync.pageTitle'
          );
        }
      });
  }

  private loadExecutions(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => {
        this.applySyncTaskExecutions(executions);
      },
      error: err => {
        this.handleAuthError(err);
      }
    });
  }

  private handleAuthError(err: HttpErrorResponse): void {
    this.errorMessage =
      err.status === UNAUTHORIZED
        ? ERROR_MESSAGES.INVALID_TOKEN
        : ERROR_MESSAGES.FETCH_FAILURE;

    this.authService.clearToken();
  }

  // Synchronize
  trigger(syncTask: SyncTaskRow): void {
    if (syncTask.key === SYNC_ONE_PRODUCT_KEY) {
      this.openSyncOneProductDialog();
      return;
    }

    this.runSyncTask(syncTask);
  }

  private runSyncTask(syncTask: SyncTaskRow): void {
    this.setSyncTaskRunning(syncTask);
    this.logStream.resetTask(syncTask.key);
    this.syncTaskTriggers[syncTask.key]()
      .pipe(finalize(() => (this.loadingSyncTaskKey = null)))
      .subscribe({
        next: (execution: unknown) => this.handleSyncTaskSuccess(syncTask, execution as SyncTaskExecution),
        error: () => this.handleSyncTaskFailure(syncTask)
      });
  }

  private setSyncTaskRunning(syncTask: SyncTaskRow): void {
  const now = new Date();

  this.loadingSyncTaskKey = syncTask.key;

  Object.assign(syncTask, {
    status: SyncTaskStatus.RUNNING,
    triggeredAt: syncTask.completedAt ?? null, // last run
    completedAt: null,
    message: null
  });
}

  private handleSyncTaskSuccess(syncTask: SyncTaskRow, execution?: SyncTaskExecution): void {
    // If backend returned an execution for this task, apply it immediately
    const now = new Date();

  // ✅ Immediate UI update (important)
  syncTask.status = execution?.status ?? SyncTaskStatus.SUCCESS;
  syncTask.completedAt = now;
  syncTask.message = execution?.message ?? syncTask.message;

  // Prefer backend timestamps if available
  if (execution?.triggeredAt) {
    syncTask.triggeredAt = new Date(execution.triggeredAt);
  }
  if (execution?.completedAt) {
    syncTask.completedAt = new Date(execution.completedAt);
  }

  // 🔄 Background refresh (do not block UI)
  this.reloadExecutions();
  }

  private handleSyncTaskFailure(syncTask: SyncTaskRow): void {
    this.reloadExecutions();
  }

  private reloadExecutions(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => {
        // Apply executions once. preserve existing `triggeredAt` if backend
        // does not provide one (prevents losing the value set when the
        // task was started). Log API response for debugging.
        console.log('API executions', executions);
        this.applySyncTaskExecutions(executions);
        console.log('After apply', this.syncTasks);
      },

      error: err => this.handleAuthError(err)
    });
  }

  private applySyncTaskExecutions(executions: SyncTaskExecution[]): void {
    this.syncTasks = this.syncTasks.map(task => {
    const execution = executions.find(e => e.key === task.key);
    if (!execution) return task;

    return {
      ...task,
      status: execution.status ?? task.status,

      // ✅ Only override if backend provides value
      triggeredAt: execution.triggeredAt
        ? new Date(execution.triggeredAt)
        : task.triggeredAt,

      completedAt: execution.completedAt
        ? new Date(execution.completedAt)
        : task.completedAt,

      message: execution.message ?? task.message
    };
  });
  }

  getStatusClass(status?: SyncTaskStatus): string {
    switch (status) {
      case SyncTaskStatus.SUCCESS:
        return 'text-success';
      case SyncTaskStatus.FAILED:
        return 'text-danger';
      case SyncTaskStatus.RUNNING:
        return 'text-warning';
      default:
        return '';
    }
  }

  // Synchronize one product dialog
  private openSyncOneProductDialog(): Observable<void> {
  return this.productService.fetchAllProductsForSync().pipe(
    tap(products => {
      this.products = products;
      this.filteredProducts = products.slice(0, 10);
      this.showSyncOneProductDialog = true;
    }),
    map(() => void 0)
  );
}

  confirmSyncOneProduct(): void {
    const syncTask = this.syncTasks.find(t => t.key === SYNC_ONE_PRODUCT_KEY);
    if (!syncTask || !this.isValidSyncOneProductValues()) {
      this.markSyncOneProductFailed(syncTask);
      return;
    }

    this.executeSyncOneProduct(syncTask);
  }

  isValidSyncOneProductValues(): boolean {
    const matchedProduct = this.products.some(
      product => product.id === this.productSearch
    );
    return matchedProduct && !!this.marketDirectory;
  }

  private executeSyncOneProduct(syncTask: SyncTaskRow): void {
    this.setSyncTaskRunning(syncTask);
    this.logStream.resetTask(syncTask.key);
    this.showSyncOneProductDialog = false;

    this.service
      .syncOneProduct(
        this.productSearch,
        this.marketDirectory.trim(),
        this.overrideMarketItemPath
      )
      .pipe(finalize(() => (this.loadingSyncTaskKey = null)))
      .subscribe({
        next: (execution: unknown) => this.handleSyncTaskSuccess(syncTask, execution as SyncTaskExecution),
        error: () => this.handleSyncTaskFailure(syncTask)
      });
  }

  private markSyncOneProductFailed(syncTask?: SyncTaskRow): void {
    if (!syncTask) {
      return;
    }

    Object.assign(syncTask, {
      status: SyncTaskStatus.FAILED,
      completedAt: new Date(),
      message: this.translateService.instant(
        'common.admin.sync.syncOneProductDialog.validationMessage'
      )
    });
  }

  cancelSyncOneProduct(): void {
    this.showSyncOneProductDialog = false;
    this.productSearch = '';
    this.marketDirectory = '';
    this.overrideMarketItemPath = false;
  }

  // Product search dropdown in sync one product dialog
  openDropdown(): void {
    this.dropdownOpen = true;
    this.filteredProducts = this.products.slice(0, 10);
  }

  filterProducts(): void {
    const value = this.productSearch.toLowerCase();

    this.filteredProducts = this.products
      .filter(product => product.id.toLowerCase().includes(value))
      .slice(0, 10);

    // Clear the market directory if ID input does not match any product IDs
    if (!this.isValidSyncOneProductValues()) {
      this.marketDirectory = '';
    }

    this.dropdownOpen = true;
  }

  selectProduct(product: MarketProduct): void {
    this.productSearch = product.id;
    this.marketDirectory = product.marketDirectory ?? '';
    this.dropdownOpen = false;
  }

  openLog(syncTask: SyncTaskRow) {
    this.selectedTask.set(syncTask);

    const hasLogs = this.logStream.hasLogs(syncTask.key);
    const isRunning = syncTask.status === SyncTaskStatus.RUNNING;

    const showLogsUI = isRunning || hasLogs;

    const modalRef = this.modalService.open(this.changelogContent, {
      size: showLogsUI ? 'xl' : 'md',
      centered: true,
      windowClass: showLogsUI ? 'log-modal has-logs' : 'log-modal no-logs',
      modalDialogClass: showLogsUI ? 'log-modal has-logs' : 'log-modal no-logs'
    });

    modalRef.shown.subscribe(() => {
      if (isRunning) {
        this.logStream.connectTask(syncTask.key);
      }

      setTimeout(() => {
        globalThis.dispatchEvent(new Event('resize'));
      }, 0);
    });
  }

  toggleExpand(index: number): void {
    const current = new Set(this.expandedLogs());
    if (current.has(index)) {
      current.delete(index);
    } else {
      current.add(index);
    }
    this.expandedLogs.set(current);
  }

  isExpanded(index: number): boolean {
    return this.expandedLogs().has(index);
  }
}
