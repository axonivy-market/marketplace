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
import { FEEDBACK_REVIEW_TABS } from "../../shared/constants/common.constant";
import { FeedbackStatus } from "../../shared/enums/feedback-status.enum";

@Component({
  selector: 'app-feedback-review',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule
  ],
  templateUrl: './feedback-review.component.html',
  styleUrls: ['./feedback-review.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackReviewComponent {
  authService = inject(AuthService);
  appModalService = inject(AppModalService);
  productFeedbackService = inject(ProductFeedbackService);
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);

  detailTabs = FEEDBACK_REVIEW_TABS;
  activeTab = 'review';

  // showToggle = computed(() => this.scrollHeight() > this.clientHeight() || this.feedback.isExpanded);
  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

  ngOnInit(): void {
    console.log("Initializing FeedbackReviewComponent...");

    if (!this.authService.getToken()) {
      console.log("User not authenticated, redirecting to GitHub login...");
      this.authService.redirectToGitHub('feedback-review');
      // return;
    }

      // Load feedbacks initially
      this.productFeedbackService.findProductFeedbacks().subscribe(response => {
        console.log("Feedbacks loaded:", response._embedded.feedbacks);

        // Set all feedbacks
        this.productFeedbackService.allFeedbacks.set(response._embedded.feedbacks.sort((a, b) =>
          (b.reviewDate ? new Date(b.reviewDate).getTime() : 0) -
          (a.reviewDate ? new Date(a.reviewDate).getTime() : 0)
        ));

        // Filter and set pending feedbacks
        this.productFeedbackService.pendingFeedbacks.set(
          response._embedded.feedbacks.filter(fb => fb.feedbackStatus === FeedbackStatus.PENDING).sort((a, b) =>
            (b.updatedAt ? new Date(b.updatedAt).getTime() : 0) -
            (a.updatedAt ? new Date(a.updatedAt).getTime() : 0)
          )
        );
      });

  }

  onClickingApproveButton(feedback: Feedback): void {
    console.log(feedback);
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, true, this.authService.getDisplayName()!).subscribe(response => {
      console.log("Feedback approved:", response);
    });
  }

  onClickingRejectButton(feedback: Feedback): void {
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, false, this.authService.getDisplayName()!).subscribe(response => {
      console.log("Feedback rejected:", response);
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
