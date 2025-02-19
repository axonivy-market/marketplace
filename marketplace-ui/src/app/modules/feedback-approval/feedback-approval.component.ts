import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CommonDropdownComponent } from "../../shared/components/common-dropdown/common-dropdown.component";
import { TranslateModule } from "@ngx-translate/core";
import { MultilingualismPipe } from "../../shared/pipes/multilingualism.pipe";
import { Component, inject, Signal, ViewEncapsulation } from "@angular/core";
import { AuthService } from "../../auth/auth.service";
import { AppModalService } from "../../shared/services/app-modal.service";
import { Feedback } from "../../shared/models/feedback.model";
import { ProductFeedbackService } from "../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service";
import { TimeAgoPipe } from "../../shared/pipes/time-ago.pipe";
import { LanguageService } from "../../core/services/language/language.service";
import { ThemeService } from "../../core/services/theme/theme.service";

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

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.allFeedbacks;

    // showToggle = computed(() => this.scrollHeight() > this.clientHeight() || this.feedback.isExpanded);

  ngOnInit(): void {
    console.log("Initializing FeedbackApprovalComponent...");

    if (!this.authService.getToken()) {
      console.log("User not authenticated, redirecting to GitHub login...");
      this.authService.redirectToGitHub('feedback-approval');
    }

    this.productFeedbackService.getInitAllFeedbacksObservable().subscribe(response => {
      console.log("Feedbacks loaded:", response._embedded.feedbacks);
      this.productFeedbackService.allFeedbacks.set(response._embedded.feedbacks);
    });
  }

    onClickingApproveButton(): void {
      this.productFeedbackService.getInitAllFeedbacksObservable().subscribe(response => {
        console.log("Feedbacks loaded:", response._embedded.feedbacks);
        this.productFeedbackService.allFeedbacks.set(response._embedded.feedbacks);
      });
    }

    onClickingRejectButton(): void {
      this.productFeedbackService.getInitAllFeedbacksObservable().subscribe(response => {
        console.log("Feedbacks loaded:", response._embedded.feedbacks);
        this.productFeedbackService.allFeedbacks.set(response._embedded.feedbacks);
      });
    }
}
