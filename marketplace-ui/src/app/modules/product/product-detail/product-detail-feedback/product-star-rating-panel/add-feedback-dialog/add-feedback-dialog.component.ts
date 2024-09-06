import { CommonModule } from '@angular/common';
import { Component, inject, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../../../../auth/auth.service';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { MultilingualismPipe } from '../../../../../../shared/pipes/multilingualism.pipe';
import { AppModalService } from '../../../../../../shared/services/app-modal.service';
import { ProductDetailService } from '../../../product-detail.service';
import { ProductFeedbackService } from '../../product-feedbacks-panel/product-feedback.service';
import { throwError } from 'rxjs';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';

@Component({
  selector: 'app-add-feedback-dialog',
  standalone: true,
  templateUrl: './add-feedback-dialog.component.html',
  styleUrl: './add-feedback-dialog.component.scss',
  imports: [
    CommonModule,
    StarRatingComponent,
    FormsModule,
    TranslateModule,
    MultilingualismPipe,
    CommonDropdownComponent
  ]
})
export class AddFeedbackDialogComponent {
  productFeedbackService = inject(ProductFeedbackService);
  productDetailService = inject(ProductDetailService);
  activeModal = inject(NgbActiveModal);
  languageService = inject(LanguageService);
  private readonly authService = inject(AuthService);
  private readonly appModalService = inject(AppModalService);

  displayName = '';
  feedback!: Feedback;

  userFeedback: Signal<Feedback | null> =
    this.productFeedbackService.userFeedback;

  ngOnInit(): void {
    const displayName = this.authService.getDisplayName();
    if (displayName) {
      this.displayName = displayName;
    }
    this.feedback = {
      content: this.userFeedback()?.content ?? '',
      rating: this.userFeedback()?.rating ?? 0,
      productId: this.productDetailService.productId()
    };
  }

  onSubmitFeedback(): void {
    console.log(54);
    this.productFeedbackService.submitFeedback(this.feedback).subscribe({
      complete: () => {
        this.activeModal.close();
        this.appModalService.openSuccessDialog();
      },
      error: error => throwError(() => error)
    });
  }

  onRateChange(newRate: number) {
    console.log(55);
    this.feedback.rating = newRate;
  }

}
