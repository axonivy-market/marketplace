import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../../../auth/auth.service';
import { LanguageService } from '../../../../../../core/services/language/language.service';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { MultilingualismPipe } from '../../../../../../shared/pipes/multilingualism.pipe';
import { AppModalService } from '../../../../../../shared/services/app-modal.service';
import { ProductDetailService } from '../../../product-detail.service';
import { ProductFeedbackService } from '../../product-feedbacks-panel/product-feedback.service';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';
import { MAX_FEEDBACK_LENGTH, NOT_FOUND_ERROR_CODE, USER_NOT_FOUND_ERROR_CODE } from '../../../../../../shared/constants/common.constant';
import { CharacterCountPipe } from '../../../../../../shared/pipes/character-count.pipe';
import { FeedbackStatus } from '../../../../../../shared/enums/feedback-status.enum';

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
    CommonDropdownComponent,
    NgOptimizedImage,
    CharacterCountPipe
  ]
})
export class AddFeedbackDialogComponent {
  productFeedbackService = inject(ProductFeedbackService);
  productDetailService = inject(ProductDetailService);
  activeModal = inject(NgbActiveModal);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  private readonly authService = inject(AuthService);
  private readonly appModalService = inject(AppModalService);

  displayName = '';
  maxFeedbackLength = MAX_FEEDBACK_LENGTH;
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
      productId: this.productDetailService.productId(),
      feedbackStatus: this.userFeedback()?.feedbackStatus ?? FeedbackStatus.PENDING,
      moderatorId: this.userFeedback()?.moderatorId ?? ''
    };
  }

  onSubmitFeedback(): void {
    this.productFeedbackService.submitFeedback(this.feedback).subscribe({
      complete: () => {
        this.activeModal.close();
        this.appModalService.openSuccessDialog();
      },
      error: error => {
        if (
          error.status === NOT_FOUND_ERROR_CODE &&
          error.error.helpCode === USER_NOT_FOUND_ERROR_CODE.toString()
        ) {
          this.authService.redirectToGitHub(this.feedback.productId);
        }
      }
    });
  }

  onRateChange(newRate: number) {
    this.feedback.rating = newRate;
  }
}
