import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
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
  ADMIN_SESSION_TOKEN,
  UNAUTHORIZED,
  SYNC_TASKS
} from '../../shared/constants/common.constant';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ThemeService } from '../../core/services/theme/theme.service';
import { PageTitleService } from '../../shared/services/page-title.service';
import { ProductService } from '../../modules/product/product.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { SyncTaskRow } from '../../shared/models/sync-task-execution.model';
import { MarketProduct } from '../../shared/models/product.model';
import { SyncTaskStatus } from '../../shared/enums/sync-task-status.enum';
import { AdminTokenContainerComponent } from './admin-token.component';

const SYNC_ONE_PRODUCT_KEY = 'syncOneProduct';
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    LoadingSpinnerComponent,
    AdminTokenContainerComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminDashboardComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly service = inject(AdminDashboardService);
  private readonly productService = inject(ProductService);
  private readonly storageRef = inject(SessionStorageRef);

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);

  protected LoadingComponentId = LoadingComponentId;

  token = '';
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
    this.isAuthenticated = true;

    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => {
        this.applySyncTaskExecutions(executions);
        this.pageTitleService.setTitleOnLangChange(
          'common.admin.sync.pageTitle'
        );
      },
      error: err => {
        this.handleAuthError(err);
      }
    });

    this.updateVisibility(this.router.url);

    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => this.updateVisibility(this.router.url));
  }

  private updateVisibility(url: string): void {
    this.showSyncTask = /^\/internal-dashboard\/?(\?.*)?$/.test(url);
  }

  onSubmit(token: string): void {
    this.token = token;
    this.errorMessage = '';

    if (!token) {
      this.handleMissingToken();
      return;
    }

    sessionStorage.setItem(ADMIN_SESSION_TOKEN, token);
    this.verifyTokenAndLoad();
  }

  private verifyTokenAndLoad(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => {
        this.isAuthenticated = true;
        this.applySyncTaskExecutions(executions);
        this.pageTitleService.setTitleOnLangChange(
          'common.admin.sync.pageTitle'
        );
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

    this.isAuthenticated = false;
    sessionStorage.removeItem(ADMIN_SESSION_TOKEN);
  }

  private handleMissingToken(): void {
    this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
    this.isAuthenticated = false;
  }

  // Synchronize
  async trigger(syncTask: SyncTaskRow): Promise<void> {
    if (syncTask.key === SYNC_ONE_PRODUCT_KEY) {
      await this.openSyncOneProductDialog();
      return;
    }

    this.runSyncTask(syncTask);
  }

  private runSyncTask(syncTask: SyncTaskRow): void {
    this.setSyncTaskRunning(syncTask);

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
      syncTask.triggeredAt = execution.triggeredAt
        ? new Date(execution.triggeredAt)
        : undefined;
      syncTask.completedAt = execution.completedAt
        ? new Date(execution.completedAt)
        : undefined;
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
  private async openSyncOneProductDialog(): Promise<void> {
    this.products = await this.productService.fetchAllProductsForSync();
    this.filteredProducts = this.products.slice(0, 10);
    this.showSyncOneProductDialog = true;
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
}
