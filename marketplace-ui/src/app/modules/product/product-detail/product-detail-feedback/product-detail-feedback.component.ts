import {
  Component,
  computed,
  inject,
  input,
  Signal
} from '@angular/core';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel/product-star-rating-panel.component';
import { ShowFeedbacksDialogComponent } from './show-feedbacks-dialog/show-feedbacks-dialog.component';
import { ProductFeedbacksPanelComponent } from './product-feedbacks-panel/product-feedbacks-panel.component';
import { AppModalService } from '../../../../shared/services/app-modal.service';
import { ProductFeedbackService } from './product-feedbacks-panel/product-feedback.service';
import { TranslateModule } from '@ngx-translate/core';

const MAX_ELEMENTS = 6;

@Component({
  selector: 'app-product-detail-feedback',
  standalone: true,
  imports: [
    ProductStarRatingPanelComponent,
    ShowFeedbacksDialogComponent,
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

  showPopup!: boolean;

  openShowFeedbacksDialog(): void {
    if (this.isMobileMode()) {
      this.productFeedbackService.loadMoreFeedbacks();
    } else {
      this.appModalService.openShowFeedbacksDialog();
    }
  }
}
