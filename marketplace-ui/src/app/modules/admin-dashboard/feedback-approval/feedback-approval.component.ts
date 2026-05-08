import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Component, computed, inject, OnInit, PLATFORM_ID, Signal, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { finalize } from 'rxjs';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { ProductFeedbackService } from '../../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { PageTitleService } from '../../../shared/services/page-title.service';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { Feedback } from '../../../shared/models/feedback.model';
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
  protected LoadingComponentId = LoadingComponentId;
  adminAuthService = inject(AdminAuthService);
  appModalService = inject(AppModalService);
  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  activatedRoute = inject(ActivatedRoute);
  pageTitleService = inject(PageTitleService);
  errorMessage = '';
  isAuthenticated = false;
  activeTab = 'review';
  isLoading = false;
  platformId = inject(PLATFORM_ID);
  feedbacks: Signal<Feedback[]> = this.productFeedbackService.allFeedbacks;
  pendingFeedbacks: Signal<Feedback[]> = this.productFeedbackService.pendingFeedbacks;
  allFeedbacks = computed(() => this.feedbacks());
  reviewingFeedbacks = computed(() => this.pendingFeedbacks());

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.pageTitleService.setTitleOnLangChange('common.approval.approvalTitle');
      this.fetchFeedbacks();
    }
  }

  fetchFeedbacks(): void {
    this.isLoading = true;
    this.productFeedbackService.findProductFeedbacks().subscribe({
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  onClickReviewButton(feedback: Feedback, isApproved: boolean): void {
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
        .updateFeedbackStatus(approvalRequest)
        .pipe(
          finalize(() => {
            this.isLoading = false;
          })
        )
        .subscribe();
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
