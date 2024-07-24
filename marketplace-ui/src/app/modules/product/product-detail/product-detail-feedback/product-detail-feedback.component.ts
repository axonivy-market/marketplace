import {
  AfterViewInit,
  Component,
  computed,
  inject,
  input,
  OnInit,
  Signal
} from '@angular/core';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel/product-star-rating-panel.component';
import { ShowFeedbacksDialogComponent } from './show-feedbacks-dialog/show-feedbacks-dialog.component';
import { ProductFeedbacksPanelComponent } from './product-feedbacks-panel/product-feedbacks-panel.component';
import { AppModalService } from '../../../../shared/services/app-modal.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductFeedbackService } from './product-feedbacks-panel/product-feedback.service';
import { AuthService } from '../../../../auth/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { ProductStarRatingService } from './product-star-rating-panel/product-star-rating.service';

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
export class ProductDetailFeedbackComponent implements OnInit, AfterViewInit {
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
  private readonly productStarRatingService = inject(ProductStarRatingService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  showPopup!: boolean;

  ngOnInit(): void {
    this.productFeedbackService.findProductFeedbackOfUser();
    this.productStarRatingService.fetchData();
  }

  ngAfterViewInit(): void {
    this.route.queryParams.subscribe(params => {
      this.showPopup = params['showPopup'] === 'true';
      if (this.showPopup && this.authService.getToken()) {
        this.appModalService.openAddFeedbackDialog().then(
          () => this.removeQueryParam(),
          () => this.removeQueryParam()
        );
      }
    });
  }

  openShowFeedbacksDialog(): void {
    if (this.isMobileMode()) {
      this.productFeedbackService.loadMoreFeedbacks();
    } else {
      this.appModalService.openShowFeedbacksDialog();
    }
  }

  private removeQueryParam(): void {
    this.router.navigate([], {
      queryParams: { showPopup: null },
      queryParamsHandling: 'merge'
    });
  }
}
