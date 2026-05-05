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
  SyncTaskExecution
} from './admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import {
  ERROR_MESSAGES,
  UNAUTHORIZED,
  } from '../../shared/constants/common.constant';
import { SYNC_TASKS, SYNC_TASK_KEYS, SyncTaskKey } from '../../shared/constants/admin.constant';
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
import { SyncTaskDialogComponent } from './components/sync-task-dialog.component';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule, SyncTaskDialogComponent],
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
  showSyncProductDialog = false;

  syncTasks = SYNC_TASKS;
  products: MarketProduct[] = [];
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
    syncZipArtifacts: () => this.service.syncZipArtifacts(),
    syncOneProduct: () => EMPTY
  };

  ngOnInit(): void {
    this.loadExecutions();
    this.pageTitleService.setTitleOnLangChange('common.admin.sync.pageTitle');
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
      if (event.urlAfterRedirects === '/internal-dashboard') {
        this.pageTitleService.setTitleOnLangChange('common.admin.sync.pageTitle');
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
    this.errorMessage = err.status === UNAUTHORIZED ? ERROR_MESSAGES.INVALID_TOKEN : ERROR_MESSAGES.FETCH_FAILURE;

    this.authService.clearToken();
  }

  // Synchronize
  async trigger(syncTask: SyncTaskRow): Promise<void> {
    this.selectedTask.set(syncTask);
    if (syncTask.key === SYNC_TASK_KEYS.SYNC_ONE_PRODUCT || syncTask.key === SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS) {
      await this.openSyncProductDialog();
      return;
    }
    this.executeTask(syncTask, this.syncTaskTriggers[syncTask.key]());
  }

  private setSyncTaskRunning(syncTask: SyncTaskRow): void {
    this.loadingSyncTaskKey = syncTask.key;
    Object.assign(syncTask, {
      status: SyncTaskStatus.RUNNING,
      triggeredAt: new Date(),
      completedAt: null,
      message: null
    });
  }

  private handleSyncTaskSuccess(syncTask: SyncTaskRow): void {
    Object.assign(syncTask, {
      status: SyncTaskStatus.SUCCESS,
      completedAt: new Date(),
      message: 'Success'
    });

    this.reloadExecutions();
  }

  private handleSyncTaskFailure(syncTask: SyncTaskRow): void {
    Object.assign(syncTask, {
      status: SyncTaskStatus.FAILED,
      completedAt: new Date(),
      message: 'Failed'
    });

    if (this.isAuthenticated) {
      this.reloadExecutions();
    }
  }

  private reloadExecutions(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => this.applySyncTaskExecutions(executions),
      error: err => this.handleAuthError(err)
    });
  }

  private applySyncTaskExecutions(executions: SyncTaskExecution[]): void {
    executions.forEach(execution => {
      const syncTask = this.syncTasks.find(t => t.key === execution.key);
      if (!syncTask) {
        return;
      }

      syncTask.status = execution.status;
      syncTask.triggeredAt = execution.triggeredAt ? new Date(execution.triggeredAt) : undefined;
      syncTask.completedAt = execution.completedAt ? new Date(execution.completedAt) : undefined;
      syncTask.message = execution.message ?? undefined;
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
  private async openSyncProductDialog(): Promise<void> {
    this.products = await this.productService.fetchAllProductsForSync();
    this.showSyncProductDialog = true;
  }

  private markSyncOneProductFailed(syncTask?: SyncTaskRow): void {
    if (!syncTask) {
      return;
    }

    Object.assign(syncTask, {
      status: SyncTaskStatus.FAILED,
      completedAt: new Date(),
      message: this.translateService.instant('common.admin.sync.syncProductDialog.validationMessage')
    });
  }

  cancelSyncProduct(): void {
    this.showSyncProductDialog = false;
    this.selectedTask.set(null);
    this.productSearch = '';
    this.marketDirectory = '';
    this.overrideMarketItemPath = false;
  }

  // Product search dropdown in sync one product dialog
  openDropdown(): void {
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

  handleSyncDialogConfirm(event: { productId: string; marketDirectory: string; override: boolean }): void {
    const task = this.selectedTask();
    if (!task) {
      return;
    }
    if (task.key === SYNC_TASK_KEYS.SYNC_ONE_PRODUCT) {
      const isValid = this.products.some(p => p.id === event.productId) && !!event.marketDirectory;

      if (!isValid) {
        this.markSyncOneProductFailed(task);
        return;
      }
      this.executeTask(task, this.service.syncOneProduct(event.productId, event.marketDirectory, event.override));
    } else {
      this.executeTask(task, this.service.syncZipArtifacts(false, event.productId));
    }
  }

  private executeTask(task: SyncTaskRow, request$: Observable<unknown>): void {
    this.setSyncTaskRunning(task);
    this.logStream.resetTask(task.key);
    this.showSyncProductDialog = false;

    request$.pipe(finalize(() => (this.loadingSyncTaskKey = null))).subscribe({
      next: () => this.handleSyncTaskSuccess(task),
      error: () => this.handleSyncTaskFailure(task)
    });
  }
}
