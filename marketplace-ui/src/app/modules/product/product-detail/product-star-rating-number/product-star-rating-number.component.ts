import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating.component';
import {
  ProductStarRatingService
} from '../product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../auth/auth.service';
import { ProductDetailService } from '../product-detail.service';
import { LanguageService } from '../../../../core/services/language/language.service';

@Component({
  selector: 'app-product-star-rating-number',
  standalone: true,
  imports: [CommonModule, TranslateModule, StarRatingComponent],
  templateUrl: './product-star-rating-number.component.html',
  styleUrl: './product-star-rating-number.component.scss'
})
export class ProductStarRatingNumberComponent {
  productStarRatingService = inject(ProductStarRatingService);
  private readonly productDetailService = inject(ProductDetailService);
  private readonly authService = inject(AuthService);
  languageService = inject(LanguageService);

  @Input() isShowRateLink = true;
  @Input() isShowTotalRatingNumber = true;
  @Input() ratingBtnLabel = '';
  @Output() openAddFeedbackDialog = new EventEmitter<void>();

  onClickRateLink() {
    const productId = this.productDetailService.productId();
    if(this.authService.getToken()) {
      this.openAddFeedbackDialog.emit();
    }
    else {
      this.authService.redirectToGitHub(productId);
    }
  }
}
