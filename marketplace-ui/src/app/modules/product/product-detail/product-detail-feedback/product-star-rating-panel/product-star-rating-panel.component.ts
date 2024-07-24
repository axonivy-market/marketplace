import { Component, EventEmitter, inject, Input, input, Output, Signal } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductStarRatingService } from './product-star-rating.service';

import { StarRatingHighlightDirective } from './star-rating-highlight.directive';
import {
  ProductStarRatingNumberComponent
} from '../../product-star-rating-number/product-star-rating-number.component';
import { CommonModule } from '@angular/common';
import { StarRatingCounting } from '../../../../../shared/models/star-rating-counting.model';

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

  @Input() isRenderInModalDialog = false;
  @Output() openAddFeedbackDialog = new EventEmitter<void>();

  productStarRatingService = inject(ProductStarRatingService);

  starRatings: Signal<StarRatingCounting[]> = this.productStarRatingService.starRatings;
}
