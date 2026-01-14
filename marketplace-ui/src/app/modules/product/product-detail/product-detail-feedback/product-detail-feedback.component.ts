import {
  Component,
  computed,
  inject,
  input,
  Signal
} from '@angular/core';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel/product-star-rating-panel.component';
import { ProductFeedbacksPanelComponent } from './product-feedbacks-panel/product-feedbacks-panel.component';
import { AppModalService } from '../../../../shared/services/app-modal.service';
import { ProductFeedbackService } from './product-feedbacks-panel/product-feedback.service';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';

const MAX_ELEMENTS = 6;

@Component({
  selector: 'app-product-detail-feedback',
  imports: [
    ProductStarRatingPanelComponent,
    ProductFeedbacksPanelComponent,
    TranslateModule
  ],
  templateUrl: './product-detail-feedback.component.html',
  styleUrls: ['./product-detail-feedback.component.scss']
})
export class ProductDetailFeedbackComponent {
  isMobileMode = input<boolean>();
  isShowBtnMore: Signal<boolean> = computed(() => {
    if (
      this.productFeedbackService.areAllFeedbacksLoaded() &&
      (this.isMobileMode() ||
        this.productFeedbackService.totalElements() <= MAX_ELEMENTS)
    ) {
      return false;
    }
    return true;
  });

  productFeedbackService = inject(ProductFeedbackService);
  appModalService = inject(AppModalService);
  languageService = inject(LanguageService);

  showPopup!: boolean;

  openShowFeedbacksDialog(): void {
    if (this.isMobileMode()) {
      this.productFeedbackService.loadMoreFeedbacks();
    } else {
      this.appModalService.openShowFeedbacksDialog();
    }
  }
}
