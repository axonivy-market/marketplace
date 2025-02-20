import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CommonDropdownComponent } from "../../shared/components/common-dropdown/common-dropdown.component";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MultilingualismPipe } from "../../shared/pipes/multilingualism.pipe";
import { Component, computed, inject, Signal, ViewEncapsulation } from "@angular/core";
import { AuthService } from "../../auth/auth.service";
import { AppModalService } from "../../shared/services/app-modal.service";
import { Feedback } from "../../shared/models/feedback.model";
import { ProductFeedbackService } from "../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service";
import { TimeAgoPipe } from "../../shared/pipes/time-ago.pipe";
import { LanguageService } from "../../core/services/language/language.service";
import { ThemeService } from "../../core/services/theme/theme.service";
import { ItemDropdown } from "../../shared/models/item-dropdown.model";
import { APPROVAL_TABS } from "../../shared/constants/common.constant";
import { log } from "console";
import { FeedbackStatus } from "../../shared/enums/feedback-status.enum";

@Component({
  selector: 'app-feedback-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    MultilingualismPipe,
    TimeAgoPipe
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

  detailTabs = APPROVAL_TABS;
  activeTab = 'review'; // Default to 'review'
  // displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
  //   this.languageService.selectedLanguage();

  //   return this.getDisplayedTabsSignal();
  // });
  // showToggle = computed(() => this.scrollHeight() > this.clientHeight() || this.feedback.isExpanded);
  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

  pendingFeedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.pendingFeedbacks;

  ngOnInit(): void {
    console.log("Initializing FeedbackApprovalComponent...");

    if (!this.authService.getToken()) {
      console.log("User not authenticated, redirecting to GitHub login...");
      this.authService.redirectToGitHub('feedback-review');
      // return;
    } else {

      // Load feedbacks initially
      this.productFeedbackService.findProductFeedbacks().subscribe(response => {
        console.log("Feedbacks loaded:", response._embedded.feedbacks);

        // Set all feedbacks
        this.productFeedbackService.allFeedbacks.set(response._embedded.feedbacks);

        // Filter and set pending feedbacks
        this.productFeedbackService.pendingFeedbacks.set(
          response._embedded.feedbacks.filter(fb => fb.feedbackStatus === FeedbackStatus.PENDING)
        );
      });
    }

  }

  onClickingApproveButton(feedback: Feedback): void {
    console.log(feedback);
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, true, this.authService.getUserId()!).subscribe(response => {
      console.log("Feedback approved:", response);
    });
  }

  onClickingRejectButton(feedback: Feedback): void {
    this.productFeedbackService.updateFeedbackStatus(feedback.id!, false, this.authService.getUserId()!).subscribe(response => {
      console.log("Feedback rejected:", response);
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }
}
