import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, ViewChild, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter, finalize } from 'rxjs';
import { AdminDashboardService, SyncResponse, SyncJobExecutionDto, SyncJobStatus, SyncJobKey } from './admin-dashboard.service';
import { SideMenuComponent } from '../../shared/components/side-menu/side-menu.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../core/services/language/language.service';
import {
  ERROR_MESSAGES,
  FEEDBACK_APPROVAL_SESSION_TOKEN,
  UNAUTHORIZED
} from '../../shared/constants/common.constant';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ThemeService } from '../../core/services/theme/theme.service';
import { API_URI } from '../../shared/constants/api.constant';

interface SyncJobRow {
  key: SyncJobKey;
  label: string;
  status?: SyncJobStatus;
  triggeredAt?: Date;
  completedAt?: Date;
  message?: string;
  reference?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderComponent,
    SideMenuComponent,
    RouterModule,
    TranslateModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})

export class AdminDashboardComponent implements OnInit {
  service = inject(AdminDashboardService);
  private readonly router = inject(Router);
  token = '';
  errorMessage = '';
  isAuthenticated = false;
  isLoading = false;
  isSidebarOpen = false;
  jobs: SyncJobRow[] = [
    { key: 'syncProducts', label: 'Sync all products' },
    { key: 'syncOneProduct', label: 'Sync one product' },
    { key: 'syncLatestReleasesForProducts', label: 'Sync product release notes' },
    { key: 'syncGithubMonitor', label: 'Sync GitHub monitor' }
  ];
  productId = '';
  marketItemPath = '';
  overrideMarketItemPath = false;
  API_URI = API_URI; // expose constants for template
  showSyncJob = true;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  @ViewChild(SideMenuComponent) private readonly sideMenu?: SideMenuComponent;

  constructor(private readonly storageRef: SessionStorageRef) {}
  ngOnInit() {
    try {
      const storedSidebarState = localStorage.getItem('sidebarOpen');
      if (storedSidebarState !== null) {
        this.isSidebarOpen = storedSidebarState === 'true';
      }
    } catch {}
    this.token =
      this.storageRef.session?.getItem(FEEDBACK_APPROVAL_SESSION_TOKEN) ?? '';
    if (this.token) {
      this.isAuthenticated = true;
      this.fetchFeedbacks();
    }
  }

  private updateVisibility(url: string) {
    // Always show jobs table now
     this.showSyncJob = /^\/octopus\/?(\?.*)?$/.test(url);
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }
    // For now treat any non-empty token as authenticated (no server validation yet)
    this.isAuthenticated = true;
    this.fetchFeedbacks();
  }

  fetchFeedbacks(): void {
    this.isLoading = true;
    sessionStorage.setItem(FEEDBACK_APPROVAL_SESSION_TOKEN, this.token);
    // If future validation is needed, call an auth endpoint here and set isAuthenticated accordingly.
    this.updateVisibility(this.router.url);
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => {
        this.updateVisibility(this.router.url);
      });
    this.isLoading = false;
    this.loadSyncJobExecutions();
  }

  trigger(job: SyncJobRow) {
    job.status = 'RUNNING';
    job.triggeredAt = new Date();
    job.completedAt = undefined;
    job.message = undefined;
    job.reference = undefined;
    console.log('Triggering job:', job.key);

    if (job.key === 'syncProducts') {
      this.isLoading = true;
      this.service
        .syncProducts()
        .pipe(
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe({
          next: res => this.handleSuccess(job, res),
          error: err => {
            const shouldReload = !(err instanceof HttpErrorResponse) || err.status !== UNAUTHORIZED;
            this.handleFailure(job, err, shouldReload);
            this.handleError(err);
          }
        });
    } else if (job.key === 'syncOneProduct') {
      if (!this.productId || !this.marketItemPath) {
        job.status = 'FAILED';
        job.completedAt = new Date();
        job.message = 'Product ID & path required';
        return;
      }
      this.service
        .syncOneProduct(this.productId.trim(), this.marketItemPath.trim(), this.overrideMarketItemPath)
        .subscribe({
          next: res => this.handleSuccess(job, res),
          error: err => this.handleFailure(job, err)
        });
    } else if (job.key === 'syncLatestReleasesForProducts') {
      this.service.syncLatestReleasesForProducts().subscribe({
        next: () => this.handleSuccess(job, { messageDetails: 'Release notes synced' }),
        error: err => this.handleFailure(job, err)
      });
    } else if (job.key === 'syncGithubMonitor') {
      this.service.syncGithubMonitor().subscribe({
        next: res => this.handleSuccess(job, { messageDetails: res }),
        error: err => this.handleFailure(job, err)
      });
    }
  }

  private handleSuccess(job: SyncJobRow, res: SyncResponse) {
    job.completedAt = new Date();
    job.status = 'SUCCESS';
    job.message = res.messageDetails || 'Success';
    this.loadSyncJobExecutions();
  }

  private handleFailure(job: SyncJobRow, err: unknown, reload = true) {
    job.completedAt = new Date();
    job.status = 'FAILED';
    job.message = this.extractErrorMessage(err);
    if (reload && this.isAuthenticated) {
      this.loadSyncJobExecutions();
    }
  }

  private loadSyncJobExecutions(): void {
    if (!this.isAuthenticated) {
      return;
    }
    this.service.fetchSyncJobExecutions().subscribe({
      next: executions => this.applySyncJobExecutions(executions),
      error: err => this.handleError(err)
    });
  }

  private applySyncJobExecutions(executions: SyncJobExecutionDto[]): void {
    for (const execution of executions) {
      const job = this.jobs.find(j => j.key === execution.jobKey);
      if (!job) {
        continue;
      }
      job.status = execution.status;
      job.triggeredAt = execution.triggeredAt ? new Date(execution.triggeredAt) : undefined;
      job.completedAt = execution.completedAt ? new Date(execution.completedAt) : undefined;
      job.message = execution.message ?? undefined;
      job.reference = execution.reference ?? undefined;
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

  private extractErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (typeof err.error === 'string') {
        return err.error;
      }
      if (err.error && typeof err.error === 'object' && 'message' in err.error) {
        const nestedMessage = (err.error as { message?: unknown }).message;
        if (typeof nestedMessage === 'string') {
          return nestedMessage;
        }
      }
      return err.message;
    }
    if (err && typeof err === 'object' && 'message' in (err as { message?: unknown })) {
      const message = (err as { message?: unknown }).message;
      if (typeof message === 'string') {
        return message;
      }
    }
    if (typeof err === 'string') {
      return err;
    }
    return 'Failed';
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }
    this.isAuthenticated = false;
    sessionStorage.removeItem(FEEDBACK_APPROVAL_SESSION_TOKEN);
  }

  private handleMissingToken(): void {
    this.errorMessage = ERROR_MESSAGES.TOKEN_REQUIRED;
    this.isAuthenticated = false;
  }

  onMenuToggle(): void {
    if (!this.sideMenu) {
      return;
    }
    const currentState = this.sideMenu.isOpen;
    const nextState = !currentState;
    this.sideMenu.setOpen(nextState);
  }

  onSidebarStateChanged(open: boolean): void {
    this.isSidebarOpen = open;
  }
}
