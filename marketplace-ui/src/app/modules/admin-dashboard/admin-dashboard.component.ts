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
import { EMPTY, filter, finalize, Observable } from 'rxjs';
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

  private readonly syncTaskTriggers: Record<SyncTaskKey, () => Observable<unknown>> = {
    syncProducts: () => this.service.syncProducts(),
    syncLatestReleasesForProducts: () => this.service.syncLatestReleasesForProducts(),
    syncGithubMonitor: () => this.service.syncGithubMonitor(),
    syncGithubSecurityMonitor: () => this.service.syncGithubSecurityMonitor(),
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
      next: executions => this.applySyncTaskExecutions(executions),
      error: err => {
        this.handleAuthError(err);
      }
    });
  }

  private handleAuthError(err: HttpErrorResponse): void {
    this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    // Only remove token if unauthorized, other errors could be transient and token may still be valid
    if (err.status === UNAUTHORIZED) {
      this.authService.clearToken();
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
    }
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
        next: () => this.handleSyncTaskSuccess(syncTask),
        error: () => this.handleSyncTaskFailure(syncTask)
      });
  }

  private setSyncTaskRunning(syncTask: SyncTaskRow): void {
    this.loadingSyncTaskKey = syncTask.key;
    Object.assign(syncTask, {
      status: SyncTaskStatus.RUNNING,
      lastRunDate: syncTask.completedDate ?? null,
      completedDate: null,
      message: null
    });
  }

  private handleSyncTaskSuccess(syncTask: SyncTaskRow): void {
    syncTask.status = SyncTaskStatus.SUCCESS;
    syncTask.completedDate = new Date();
    this.reloadExecutions();
  }

  private handleSyncTaskFailure(syncTask: SyncTaskRow): void {
    Object.assign(syncTask, {
      status: SyncTaskStatus.FAILED,
      completedDate: new Date(),
      message: 'Failed'
    });
    this.reloadExecutions();
  }

  private reloadExecutions(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => this.applySyncTaskExecutions(executions),
      error: err => this.handleAuthError(err)
    });
  }

  private applySyncTaskExecutions(executions: SyncTaskExecution[]): void {
    this.syncTasks = this.syncTasks.map(task => {
      const matchingExecution = executions.find(execution => execution.key === task.key);
      if (!matchingExecution) {
        return task;
      }

      return {
        ...task,
        status: matchingExecution.status ?? task.status,
        lastRunDate: this.getLatestDate(task.lastRunDate, matchingExecution.lastRunDate),
        completedDate: this.getLatestDate(task.completedDate, matchingExecution.completedDate),
        message: matchingExecution.message ?? task.message
      };
    });
  }

  private getLatestDate(existingDate?: Date, incomingDate?: string): Date | undefined {
    if (!incomingDate) {
      return existingDate;
    }

    const parsedIncomingDate = new Date(incomingDate);

    if (!existingDate || parsedIncomingDate > existingDate) {
      return parsedIncomingDate;
    }

    return existingDate;
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
  private openSyncOneProductDialog(): void {
    this.productService.fetchAllProductsForSync()
      .subscribe(products => {
        this.products = products;
        this.filteredProducts = products;
        this.showSyncOneProductDialog = true;
      });
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
        next: () => this.handleSyncTaskSuccess(syncTask),
        error: () => this.handleSyncTaskFailure(syncTask)
      });
  }

  private markSyncOneProductFailed(syncTask?: SyncTaskRow): void {
    if (!syncTask) {
      return;
    }

    Object.assign(syncTask, {
      status: SyncTaskStatus.FAILED,
      completedDate: new Date(),
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
  }

  filterProducts(): void {
    const value = this.productSearch.toLowerCase();

    this.filteredProducts = this.products
      .filter(product => product.id.toLowerCase().includes(value));

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
