import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { EMPTY, filter, finalize, Observable } from 'rxjs';
import {
  AdminDashboardService,
  SyncTaskExecution,
  SyncTaskStatus,
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
import { API_URI } from '../../shared/constants/api.constant';
import { PageTitleService } from '../../shared/services/page-title.service';
import { ProductService } from '../../modules/product/product.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { SyncTaskRow } from '../../shared/models/sync-task-execution.model';
import { MarketProduct } from '../../shared/models/product.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    TranslateModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminDashboardComponent implements OnInit {
  private readonly router = inject(Router);

  service = inject(AdminDashboardService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  pageTitleService = inject(PageTitleService);
  productService = inject(ProductService);
  protected LoadingComponentId = LoadingComponentId;

  token = '';
  errorMessage = '';
  isAuthenticated = false;
  loadingJobKey: SyncTaskKey | null = null;
  jobs = SYNC_TASKS;
  overrideMarketItemPath = false;
  API_URI = API_URI;
  showSyncTask = true;
  showSyncOneProductDialog = false;

  products: MarketProduct[] = [];
  marketDirectory = '';
  productSearch = '';
  dropdownOpen = false;
  filteredProducts: MarketProduct[] = [];

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

  constructor(private readonly storageRef: SessionStorageRef) {}

  ngOnInit() {
    this.token = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? '';
    if (this.token) {
      this.isAuthenticated = true;
      this.loadSyncTaskExecutions();
      this.pageTitleService.setTitleOnLangChange('common.admin.sync.pageTitle');
      this.updateVisibility(this.router.url);
      this.router.events
        .pipe(filter(e => e instanceof NavigationEnd))
        .subscribe(() => {
          this.updateVisibility(this.router.url);
        });
    }
  }

  private updateVisibility(url: string) {
    this.showSyncTask = /^\/octopus\/?(\?.*)?$/.test(url);
  }

  onSubmit(): void {
    sessionStorage.setItem(ADMIN_SESSION_TOKEN, this.token);
    this.errorMessage = '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }
    this.isAuthenticated = true;
    this.loadSyncTaskExecutions();
  }

  onProductChange(product: MarketProduct) {
    this.marketDirectory = product?.marketDirectory ?? '';
  }

  openDropdown() {
    this.dropdownOpen = true;
    this.filteredProducts = this.products.slice(0, 10);
  }

  filterProducts() {
    const value = this.productSearch.toLowerCase();

    this.filteredProducts = this.products
      .filter(p => p.id.toLowerCase().includes(value))
      .slice(0, 10);

    this.dropdownOpen = true;
  }

  selectProduct(product: MarketProduct) {
    this.productSearch = product.id;
    this.marketDirectory = product.marketDirectory ?? '';

    this.dropdownOpen = false;
  }

  async trigger(job: SyncTaskRow) {
    if (job.key === 'syncOneProduct') {
      this.products = await this.productService.fetchAllProductsForSync();
      this.showSyncOneProductDialog = true;
      this.loadingJobKey = null;
      return;
    }

    this.loadingJobKey = job.key;

    Object.assign(job, {
      status: 'RUNNING',
      triggeredAt: new Date(),
      completedAt: null,
      message: null
    });

    const action = this.syncTaskTriggers[job.key];

    action()
      .pipe(finalize(() => (this.loadingJobKey = null)))
      .subscribe({
        next: () => this.handleSuccess(job),
        error: err => this.handleFailure(job, err)
      });
  }

  confirmSyncOneProduct(): void {
    const job = this.jobs.find(j => j.key === 'syncOneProduct');
    if (!job) {
      return;
    }

    if (!this.productSearch || !this.marketDirectory) {
      Object.assign(job, {
        status: 'FAILED',
        completedAt: new Date(),
        message: 'Product ID & marketItemPath required'
      });
      return;
    }
    this.loadingJobKey = job.key;
    this.showSyncOneProductDialog = false;
    Object.assign(job, {
      status: 'RUNNING',
      triggeredAt: new Date()
    });
    this.service
      .syncOneProduct(
        this.productSearch.trim(),
        this.marketDirectory.trim(),
        this.overrideMarketItemPath
      )
      .pipe(
        finalize(() => {
          this.loadingJobKey = null;
        })
      )
      .subscribe({
        next: () => {
          this.handleSuccess(job);
        },
        error: err => {
          this.showSyncOneProductDialog = false;
          this.handleFailure(job, err);
        }
      });
  }

  cancelSyncOneProduct(): void {
    this.showSyncOneProductDialog = false;
    this.productSearch = '';
    this.marketDirectory = '';
  }

  private handleSuccess(job: SyncTaskRow) {
    Object.assign(job, {
      status: 'SUCCESS',
      completedAt: new Date(),
      message: 'Success'
    });

    this.loadSyncTaskExecutions();
  }

  private handleFailure(job: SyncTaskRow, reload = true) {
    Object.assign(job, {
      status: 'FAILED',
      completedAt: new Date(),
      message: 'Failed'
    });
    if (reload && this.isAuthenticated) {
      this.loadSyncTaskExecutions();
    }
  }

  private loadSyncTaskExecutions(): void {
    this.service.fetchSyncTaskExecutions().subscribe({
      next: executions => this.applySyncTaskExecutions(executions),
      error: err => this.handleError(err)
    });
  }

  private applySyncTaskExecutions(executions: SyncTaskExecution[]): void {
    for (const execution of executions) {
      const job = this.jobs.find(j => j.key === execution.key);
      if (job) {
        job.status = execution.status;
        job.triggeredAt = execution.triggeredAt
          ? new Date(execution.triggeredAt)
          : undefined;
        job.completedAt = execution.completedAt
          ? new Date(execution.completedAt)
          : undefined;
        job.message = execution.message ?? undefined;
      }
    }
  }

  getStatusClass(status?: SyncTaskStatus): string {
    switch (status) {
      case 'SUCCESS':
        return 'text-success';
      case 'FAILED':
        return 'text-danger';
      case 'RUNNING':
        return 'text-warning';
      default:
        return '';
    }
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }
    this.isAuthenticated = false;
    sessionStorage.removeItem(ADMIN_SESSION_TOKEN);
  }

  private handleMissingToken(): void {
    this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
    this.isAuthenticated = false;
  }
}
