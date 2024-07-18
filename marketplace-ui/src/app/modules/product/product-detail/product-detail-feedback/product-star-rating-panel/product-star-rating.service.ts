import { HttpClient } from '@angular/common/http';
import {
  computed,
  inject,
  Injectable,
  Signal,
  signal,
  WritableSignal
} from '@angular/core';
import { tap } from 'rxjs';
import { StarRatingCounting } from '../../../../../shared/models/star-rating-counting.model';
import { ProductDetailService } from '../../product-detail.service';

@Injectable({
  providedIn: 'root'
})
export class ProductStarRatingService {
  private readonly productDetailService = inject(ProductDetailService);
  private readonly http = inject(HttpClient);

  starRatings: WritableSignal<StarRatingCounting[]> = signal([]);
  totalComments: Signal<number> = computed(() =>
    this.calculateTotalComments(this.starRatings())
  );
  reviewNumber: Signal<number> = computed(() =>
    this.calculateReviewNumber(this.starRatings())
  );

  fetchData(productId: string = this.productDetailService.productId()): void {
    const requestURL = `api/feedback/product/${productId}/rating`;
    this.http
      .get<StarRatingCounting[]>(requestURL)
      .pipe(
        tap(data => {
          this.sortByStar(data);
          this.starRatings.set(data);
        })
      )
      .subscribe();
  }

  private sortByStar(starRatings: StarRatingCounting[]): void {
    starRatings.sort((a, b) => b.starRating - a.starRating);
  }

  private calculateTotalComments(starRatings: StarRatingCounting[]): number {
    let totalComments = 0;
    starRatings.forEach(starRating => {
      totalComments += starRating.commentNumber ?? 0;
    });
    return totalComments;
  }

  private calculateReviewNumber(starRatings: StarRatingCounting[]): number {
    let reviewNumber = 0;
    const totalComments = this.calculateTotalComments(starRatings);
    starRatings.forEach(starRating => {
      reviewNumber += starRating.starRating * (starRating.commentNumber ?? 1);
    });
    if (totalComments > 0) {
      reviewNumber = reviewNumber / this.calculateTotalComments(starRatings);
    }

    return Math.round(reviewNumber * 10) / 10;
  }
}
