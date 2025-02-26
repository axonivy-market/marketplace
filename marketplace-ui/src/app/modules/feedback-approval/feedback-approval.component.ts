import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { Component, inject, Signal, ViewEncapsulation } from "@angular/core";
import { AuthService } from "../../auth/auth.service";
import { AppModalService } from "../../shared/services/app-modal.service";
import { Feedback } from "../../shared/models/feedback.model";
import { ProductFeedbackService } from "../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service";
import { LanguageService } from "../../core/services/language/language.service";
import { ThemeService } from "../../core/services/theme/theme.service";
import { FEEDBACK_APPROVAL_TABS } from "../../shared/constants/common.constant";
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-feedback-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule
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
  isAuthenticated = false;
  detailTabs = FEEDBACK_APPROVAL_TABS;
  activeTab = 'review';

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

  ngOnInit(): void {
    let token = this.authService.getToken();
    if(token) {
      this.fetchFeedbacks();
    } 
    else {
      this.authService.redirectToGitHub('feedback-approval');
    }
  }

  private fetchFeedbacks(): void {
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
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, isApproved, this.authService.getDisplayName()!).subscribe();
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
