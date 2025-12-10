import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { EMPTY, filter, finalize, Observable } from 'rxjs';
import {
  AdminDashboardService,
  SyncJobExecution,
  SyncJobStatus,
  SyncJobKey
} from './admin-dashboard.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import {
  ERROR_MESSAGES,
  ADMIN_SESSION_TOKEN,
  UNAUTHORIZED,
  SYNC_JOBS
} from '../../shared/constants/common.constant';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ThemeService } from '../../core/services/theme/theme.service';
import { API_URI } from '../../shared/constants/api.constant';
import { PageTitleService } from '../../shared/services/page-title.service';
import { ProductService } from '../../modules/product/product.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';
import { SyncJobRow } from '../../shared/models/sync-job-execution.model';

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
  loadingJobKey: SyncJobKey | null = null;
  jobs = SYNC_JOBS;
  productId = '';
  marketItemPath = '';
  overrideMarketItemPath = false;
  API_URI = API_URI;
  showSyncJob = true;
  showSyncOneProductDialog = false;
  productIds: string[] = [];

  private readonly syncJobTriggers: Record<
    SyncJobKey,
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
      this.loadSyncJobExecutions();
      this.loadAllProductIds();
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
    this.showSyncJob = /^\/octopus\/?(\?.*)?$/.test(url);
  }

  onSubmit(): void {
    sessionStorage.setItem(ADMIN_SESSION_TOKEN, this.token);
    this.errorMessage = '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }
    this.isAuthenticated = true;
    this.loadSyncJobExecutions();
  }

  private async loadAllProductIds(): Promise<void> {
    this.productIds = await this.productService.fetchAllProductIds();
  }

  trigger(job: SyncJobRow) {
    this.loadingJobKey = job.jobKey;

    Object.assign(job, {
      status: 'RUNNING',
      triggeredAt: new Date(),
      completedAt: null,
      message: null
    });

    if (job.jobKey === 'syncOneProduct') {
      this.showSyncOneProductDialog = true;
      this.loadingJobKey = null;
      return;
    }

    const action = this.syncJobTriggers[job.jobKey];

    action()
      .pipe(finalize(() => (this.loadingJobKey = null)))
      .subscribe({
        next: () => this.handleSuccess(job),
        error: err => this.handleFailure(job, err)
      });
  }

  confirmSyncOneProduct(): void {
    const job = this.jobs.find(j => j.jobKey === 'syncOneProduct');
    if (!job) {
      return;
    }
    if (!this.productId || !this.marketItemPath) {
      Object.assign(job, {
        status: 'FAILED',
        completedAt: new Date(),
        message: 'Product ID & marketItemPath required'
      });
      return;
    }
    this.loadingJobKey = job.jobKey;
    Object.assign(job, {
      status: 'RUNNING',
      triggeredAt: new Date()
    });
    this.service
      .syncOneProduct(
        this.productId.trim(),
        this.marketItemPath.trim(),
        this.overrideMarketItemPath
      )
      .pipe(
        finalize(() => {
          this.loadingJobKey = null;
        })
      )
      .subscribe({
        next: () => {
          this.showSyncOneProductDialog = false;
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
  }

  private handleSuccess(job: SyncJobRow) {
    Object.assign(job, {
      status: 'SUCCESS',
      completedAt: new Date(),
      message: 'Success'
    });

    this.loadSyncJobExecutions();
  }

  private handleFailure(job: SyncJobRow, reload = true) {
    Object.assign(job, {
      status: 'FAILED',
      completedAt: new Date(),
      message: 'Failed'
    });
    if (reload && this.isAuthenticated) {
      this.loadSyncJobExecutions();
    }
  }

  private loadSyncJobExecutions(): void {
    this.service.fetchSyncJobExecutions().subscribe({
      next: executions => this.applySyncJobExecutions(executions),
      error: err => this.handleError(err)
    });
  }

  private applySyncJobExecutions(executions: SyncJobExecution[]): void {
    for (const execution of executions) {
      const job = this.jobs.find(j => j.jobKey === execution.jobKey);
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

  getStatusClass(status?: SyncJobStatus): string {
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
