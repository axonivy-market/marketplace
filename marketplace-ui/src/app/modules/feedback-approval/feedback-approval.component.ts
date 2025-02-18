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

@Component({
  selector: 'app-feedback-approval',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    MultilingualismPipe
  ],
  templateUrl: './feedback-approval.component.html',
  styleUrls: ['./feedback-approval.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class FeedbackApprovalComponent {
  authService = inject(AuthService);
  appModalService = inject(AppModalService);
  productFeedbackService = inject(ProductFeedbackService);

  feedbacks: Signal<Feedback[] | undefined> =
    this.productFeedbackService.feedbacks;

  ngOnInit(): void {
    console.log(this.feedbacks);
    if (this.authService.getToken()) {
      console.log("Here");
      // this.appModalService.openFeedbacksDialog();
    } else {
      this.authService.redirectToGitHub('feedback-approval');
    }
  }
}
