import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Component, computed, inject, OnInit, Signal, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY, catchError, finalize, of, switchMap, tap, Observable } from 'rxjs';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AuthService } from '../../../auth/auth.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { ProductFeedbackService } from '../../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { Feedback } from '../../../shared/models/feedback.model';
import { SessionStorageRef } from '../../../core/services/browser/session-storage-ref.service';
import {
  ERROR_MESSAGES,
  ADMIN_SESSION_TOKEN,
  UNAUTHORIZED,
  ERROR_PAGE_PATH
} from '../../../shared/constants/common.constant';
import { FeedbackApproval } from '../../../shared/models/feedback-approval.model';
import { AdminAuthService } from '../admin-auth.service';

@Component({
  selector: 'app-feedback-approval',
  imports: [CommonModule, FormsModule, TranslateModule, FeedbackTableComponent, LoadingSpinnerComponent],
  templateUrl: './feedback-approval.component.html',
  styleUrls: ['./feedback-approval.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackApprovalComponent implements OnInit {
  authService = inject(AuthService);
  appModalService = inject(AppModalService);
  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  activatedRoute = inject(ActivatedRoute);
  pageTitleService = inject(PageTitleService);
  adminAuthService = inject(AdminAuthService);
  router = inject(Router);
  protected LoadingComponentId = LoadingComponentId;
  token: string | null = null;
  errorMessage = '';
  isAuthenticated = false;
  activeTab = 'review';
  isLoading = false;

  feedbacks: Signal<Feedback[]> = this.productFeedbackService.allFeedbacks;
  pendingFeedbacks: Signal<Feedback[]> = this.productFeedbackService.pendingFeedbacks;

  allFeedbacks = computed(() => this.feedbacks());
  reviewingFeedbacks = computed(() => this.pendingFeedbacks());

  constructor(private readonly storageRef: SessionStorageRef) {
  }

  ngOnInit() {
    this.pageTitleService.setTitleOnLangChange('common.approval.approvalTitle');
    this.fetchFeedbacks();
  }

  fetchFeedbacks(): void {
    this.isLoading = true;
    this.productFeedbackService.findProductFeedbacks().subscribe({
      error: err => {
        this.handleError(err);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.router.navigate([ERROR_PAGE_PATH]);
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }
    this.isAuthenticated = false;
    sessionStorage.removeItem(ADMIN_SESSION_TOKEN);
  }

  onClickReviewButton(feedback: Feedback, isApproved: boolean): void {
    const token = this.adminAuthService.token;
    if (!token) {
      this.router.navigate([ERROR_PAGE_PATH]);
    } else {
      if (feedback.id && feedback.version >= 0 && feedback.userId) {
        const approvalRequest: FeedbackApproval = {
          feedbackId: feedback.id,
          version: feedback.version,
          productId: feedback.productId,
          userId: feedback.userId,
          isApproved
        };
        this.isLoading = true;
        this.productFeedbackService
          .updateFeedbackStatus(token, approvalRequest)
          .pipe(
            finalize(() => {
              this.isLoading = false;
            })
          )
          .subscribe({
            error: err => this.handleError(err)
        });
      }
    }

    // if (feedback.id && feedback.version >= 0 && feedback.userId) {
    //   const approvalRequest: FeedbackApproval = {
    //     feedbackId: feedback.id,
    //     version: feedback.version,
    //     productId: feedback.productId,
    //     userId: feedback.userId,
    //     isApproved
    //   };
    //   this.isLoading = true;
    //   this.productFeedbackService
    //     .updateFeedbackStatus(token, approvalRequest)
    //     .pipe(
    //       finalize(() => {
    //         this.isLoading = false;
    //       })
    //     )
    //     .subscribe({
    //       error: err => this.handleError(err)
    //   });
    // }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
