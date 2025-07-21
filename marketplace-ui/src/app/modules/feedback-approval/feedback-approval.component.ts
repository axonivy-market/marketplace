import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  Component,
  computed,
  inject,
  Signal,
  ViewEncapsulation
} from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { AppModalService } from '../../shared/services/app-modal.service';
import { Feedback } from '../../shared/models/feedback.model';
import { ProductFeedbackService } from '../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import {
  FEEDBACK_APPROVAL_SESSION_TOKEN,
  ERROR_MESSAGES,
  UNAUTHORIZED
} from '../../shared/constants/common.constant';
import { ActivatedRoute } from '@angular/router';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { FeedbackApproval } from '../../shared/models/feedback-approval.model';
import { SessionStorageRef } from '../../core/services/browser/session-storage-ref.service';

@Component({
  selector: 'app-feedback-approval',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, FeedbackTableComponent],
  templateUrl: './feedback-approval.component.html',
  styleUrls: ['./feedback-approval.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackApprovalComponent {
  authService = inject(AuthService);
  appModalService = inject(AppModalService);
  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  activatedRoute = inject(ActivatedRoute);

  token = '';
  errorMessage = '';
  isAuthenticated = false;
  activeTab = 'review';
  moderatorName!: string | null;
  isLoading = false;

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

  allFeedbacks = computed(() => this.feedbacks() ?? []);
  reviewingFeedbacks = computed(() => this.pendingFeedbacks() ?? []);
  constructor(private readonly storageRef: SessionStorageRef) {}

  ngOnInit() {
    this.token = this.storageRef.session?.getItem(FEEDBACK_APPROVAL_SESSION_TOKEN) ?? '';
    if (this.token) {
      this.isAuthenticated = true;
      this.fetchFeedbacks();
    }
  }

  fetchUserInfo(): void {
    this.authService.getUserInfo(this.token).subscribe({
      next: () => {
        this.authService.getDisplayNameFromAccessToken(this.token).subscribe(name => this.moderatorName = name);
      },
      error: err => {
        this.handleError(err);
      }
    });
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.fetchFeedbacks();
  }

  fetchFeedbacks(): void {
    this.isLoading = true;
    sessionStorage.setItem(FEEDBACK_APPROVAL_SESSION_TOKEN, this.token);
    this.fetchUserInfo();
    this.productFeedbackService
      .findProductFeedbacks()
      .pipe(
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: () => {
          this.isAuthenticated = true;
        },
        error: err => {
          this.handleError(err);
        }
      });
  }

  private handleError(err: HttpErrorResponse): void {
    if (err.status === UNAUTHORIZED) {
      this.errorMessage = ERROR_MESSAGES.UNAUTHORIZED_ACCESS;
    } else {
      this.errorMessage = ERROR_MESSAGES.FETCH_FAILURE;
    }

    this.isAuthenticated = false;
    sessionStorage.removeItem(FEEDBACK_APPROVAL_SESSION_TOKEN);
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
      this.productFeedbackService
        .updateFeedbackStatus(approvalRequest)
        .subscribe(() => this.fetchFeedbacks());
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
