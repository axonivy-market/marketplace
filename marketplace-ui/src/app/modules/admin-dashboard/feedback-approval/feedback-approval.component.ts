import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  Component,
  computed,
  inject,
  OnInit,
  Signal,
  ViewEncapsulation
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
import { ERROR_MESSAGES, ADMIN_SESSION_TOKEN, UNAUTHORIZED } from '../../../shared/constants/common.constant';
import { FeedbackApproval } from '../../../shared/models/feedback-approval.model';

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
  protected LoadingComponentId = LoadingComponentId;
  token = '';
  errorMessage = '';
  isAuthenticated = false;
  activeTab = 'review';
  moderatorName!: string | null;
  isLoading = false;

  feedbacks: Signal<Feedback[]> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[]> =
    this.productFeedbackService.pendingFeedbacks;

  allFeedbacks = computed(() => this.feedbacks());
  reviewingFeedbacks = computed(() => this.pendingFeedbacks());
  constructor(private readonly storageRef: SessionStorageRef) {}

  ngOnInit() {
    this.fetchFeedbacks();
    this.pageTitleService.setTitleOnLangChange('common.approval.approvalTitle');
  }

  fetchFeedbacks(): void {
    this.token = this.storageRef.session?.getItem(ADMIN_SESSION_TOKEN) ?? '';
    if (!this.token) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
      this.isAuthenticated = false;
      return;
    }

    this.isLoading = true;
    this.fetchUserInfo()
      .pipe(
        switchMap(name => {
          if (!name) {
            this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
            return EMPTY;
          }
          this.errorMessage = '';
          return this.productFeedbackService.findProductFeedbacks();
        }),
        catchError(err => {
          this.handleError(err);
          return EMPTY;
        }),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe();
  }

  fetchUserInfo(): Observable<string | null> {
    const decodedToken: any = this.authService.decodeToken(this.token);
    const accessToken = decodedToken?.accessToken;
    if (!accessToken) {
      this.handleError(new HttpErrorResponse({ status: UNAUTHORIZED }));
      return of(null);
    }

    return this.authService
      .getDisplayNameFromAccessToken(accessToken)
      .pipe(
        tap(name => {
          this.isAuthenticated = !!name;
          this.moderatorName = name;
        }),
        catchError(err => {
          this.handleError(err);
          return of(null);
        })
      );
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.errorMessage = ERROR_MESSAGES.INVALID_TOKEN;
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }
    this.isAuthenticated = false;
    this.moderatorName = null;
    sessionStorage.removeItem(ADMIN_SESSION_TOKEN);
  }

  onClickReviewButton(feedback: Feedback, isApproved: boolean): void {
    if (this.moderatorName && feedback.id && feedback.version >= 0 && feedback.userId) {
      const approvalRequest: FeedbackApproval = {
        feedbackId: feedback.id,
        moderatorName: this.moderatorName,
        version: feedback.version,
        productId: feedback.productId,
        userId: feedback.userId,
        isApproved
      };
      this.isLoading = true;
      this.productFeedbackService
        .updateFeedbackStatus(approvalRequest)
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

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
