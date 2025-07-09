import { Component, EventEmitter, Inject, inject, Input, input, Output, PLATFORM_ID, Signal } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductStarRatingService } from './product-star-rating.service';

import { StarRatingHighlightDirective } from './star-rating-highlight.directive';
import {
  ProductStarRatingNumberComponent
} from '../../product-star-rating-number/product-star-rating-number.component';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { StarRatingCounting } from '../../../../../shared/models/star-rating-counting.model';
import { LanguageService } from '../../../../../core/services/language/language.service';
import { ProductDetailService } from '../../product-detail.service';

@Component({
  selector: 'app-product-star-rating-panel',
  standalone: true,
  imports: [
    CommonModule,
    ProductStarRatingNumberComponent,
    StarRatingHighlightDirective,
    TranslateModule
  ],
  templateUrl: './product-star-rating-panel.component.html',
  styleUrl: './product-star-rating-panel.component.scss'
})
export class ProductStarRatingPanelComponent {
  isMobileMode = input<boolean>();
  isBrowser: boolean;

  @Input() isRenderInModalDialog = false;
  @Output() openAddFeedbackDialog = new EventEmitter<void>();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }  

  productStarRatingService = inject(ProductStarRatingService);
  productDetailService = inject(ProductDetailService);
  languageService = inject(LanguageService);

  starRatings: Signal<StarRatingCounting[]> = this.productStarRatingService.starRatings;
}
