import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { Observable, filter, finalize } from 'rxjs';
import { ScheduledTasksService } from './admin-dashboard.service';
import { ScheduledTaskInfo } from '../../shared/models/scheduled-task-info.model';
import { SideMenuComponent } from '../../shared/components/side-menu/side-menu.component';
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

@Component({
  selector: 'app-scheduled-tasks',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SideMenuComponent,
    RouterModule,
    TranslateModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class AdminDashboardComponent implements OnInit {
  private readonly service = inject(ScheduledTasksService);
  private readonly router = inject(Router);
  readonly refreshMs = 15000;
  tasks$!: Observable<ScheduledTaskInfo[]>;
  showScheduledTasks = true; // only for base /octopus (sync jobs)
  token = '';
  errorMessage = '';
  isAuthenticated = false;
  isLoading = false;

  languageService = inject(LanguageService);
    themeService = inject(ThemeService);
  constructor(private readonly storageRef: SessionStorageRef) {}
  ngOnInit() {
    this.token =
      this.storageRef.session?.getItem(FEEDBACK_APPROVAL_SESSION_TOKEN) ?? '';
    if (this.token) {
      this.isAuthenticated = true;
      this.fetchFeedbacks();
    }
  }

  private updateVisibility(url: string) {
    // Match exactly /octopus (optionally trailing slash or query params)
    this.showScheduledTasks = /^\/octopus\/?(\?.*)?$/.test(url);
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (!this.token) {
      this.handleMissingToken();
      return;
    }
    this.fetchFeedbacks();
  }

  fetchFeedbacks(): void {
    this.isLoading = true;
    sessionStorage.setItem(FEEDBACK_APPROVAL_SESSION_TOKEN, this.token);
    // this.fetchUserInfo();
    if (!this.isAuthenticated) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
      this.isLoading = false;
      return;
    }

    this.tasks$ = this.service.getScheduledTask();
    this.updateVisibility(this.router.url);
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe(() => {
        this.updateVisibility(this.router.url);
      });
    // this.productFeedbackService
    //   .findProductFeedbacks()
    //   .pipe(
    //     finalize(() => {
    //       this.isLoading = false;
    //     })
    //   )
    //   .subscribe({
    //     next: () => {
    //       this.isAuthenticated = true;
    //     },
    //     error: err => {
    //       this.handleError(err);
    //     }
    //   });
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
}
