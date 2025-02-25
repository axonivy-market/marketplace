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
import { FEEDBACK_APPROVAL_TABS, SECURITY_MONITOR_SESSION_KEYS } from "../../shared/constants/common.constant";
import { FeedbackStatus } from "../../shared/enums/feedback-status.enum";

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

  token = '';
  isAuthenticated = false;
  detailTabs = FEEDBACK_APPROVAL_TABS;
  activeTab = 'review';

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

    ngOnInit(): void {
      this.loadSessionData(); // Check session first
      // if (this.token) {
      //   // this.appModalService.openFeedbacksDialog();
      //   this.fetchFeedbacks();
      // } else {
      //   this.authService.redirectToGitHub('feedback-approval');
      // }
      if (!this.token) {
        console.log("User not authenticated, redirecting to GitHub login...");
        this.authService.redirectToGitHub('feedback-approval');
      }

        this.fetchFeedbacks(); // Fetch only if token exists
    
    }
  
    private loadSessionData(): void {
      this.token = sessionStorage.getItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN) ?? '';
      this.isAuthenticated = !!this.token;
    }
  
    private fetchFeedbacks(): void {
      console.log("Fetching feedbacks with token:", this.token);
      
      this.productFeedbackService.findProductFeedbacks(this.token).subscribe({
        next: (response) => {
          console.log("Feedbacks loaded:", response._embedded.feedbacks);
          this.isAuthenticated = true;
          sessionStorage.setItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN, this.token); // Persist token
        },
        error: (err) => {
          this.clearSessionData();
        }
      });
    }
    
  
    private clearSessionData(): void {
      sessionStorage.removeItem(SECURITY_MONITOR_SESSION_KEYS.TOKEN);
      this.token = '';
      this.isAuthenticated = false;
    }

  onClickingApproveButton(feedback: Feedback): void {
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, true, this.authService.getDisplayName()!).subscribe(response => {
    });
  }

  onClickingRejectButton(feedback: Feedback): void {
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, false, this.authService.getDisplayName()!).subscribe(response => {
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
