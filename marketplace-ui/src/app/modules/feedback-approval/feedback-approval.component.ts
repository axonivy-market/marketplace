import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { Component, computed, inject, Signal, ViewEncapsulation } from "@angular/core";
import { AuthService } from "../../auth/auth.service";
import { AppModalService } from "../../shared/services/app-modal.service";
import { Feedback } from "../../shared/models/feedback.model";
import { ProductFeedbackService } from "../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service";
import { LanguageService } from "../../core/services/language/language.service";
import { ThemeService } from "../../core/services/theme/theme.service";
import { FEEDBACK_APPROVAL_SESSION_TOKEN, FEEDBACK_APPROVAL_TABS, SECURITY_MONITOR_MESSAGES } from "../../shared/constants/common.constant";
import { ActivatedRoute } from "@angular/router";
import { FeedbackTableComponent } from "./feedback-table/feedback-table.component";

@Component({
  selector: 'app-feedback-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    FeedbackTableComponent
  ],
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

  token: string = '';
  errorMessage = '';
  isAuthenticated = false;
  detailTabs = FEEDBACK_APPROVAL_TABS;
  activeTab = 'review';

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

  allFeedbacks = computed(() => this.feedbacks() ?? []);
  reviewingFeedbacks = computed(() => this.pendingFeedbacks() ?? []);

  onSubmit(): void {
    this.token = this.token ?? sessionStorage.getItem(FEEDBACK_APPROVAL_SESSION_TOKEN) ?? '';
    if (!this.token) {
      this.errorMessage = SECURITY_MONITOR_MESSAGES.TOKEN_REQUIRED;
      this.isAuthenticated = false;
      return;
    }

    this.errorMessage = '';
    this.fetchFeedbacks();
  }

  fetchFeedbacks(): void {
    sessionStorage.setItem(FEEDBACK_APPROVAL_SESSION_TOKEN, this.token);
    this.productFeedbackService.findProductFeedbacks().subscribe({
      next: () => {
        this.isAuthenticated = true;
      },
      error: (err) => {
        this.isAuthenticated = false;
      }
    });
  }

  onClickReviewButton(feedback: Feedback, isApproved: boolean): void {
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, isApproved, this.authService.getDisplayName()!, feedback.version!)
      .subscribe(() => {
        this.fetchFeedbacks();
      });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
