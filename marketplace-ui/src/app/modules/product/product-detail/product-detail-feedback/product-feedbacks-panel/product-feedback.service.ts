import {
  HttpClient,
  HttpContext,
  HttpHeaders,
  HttpParams
} from '@angular/common/http';
import {
  computed,
  inject,
  Injectable,
  signal,
  WritableSignal
} from '@angular/core';
import { catchError, Observable, of, tap } from 'rxjs';
import { AuthService } from '../../../../../auth/auth.service';
import { SkipLoading } from '../../../../../core/interceptors/api.interceptor';
import { FeedbackApiResponse } from '../../../../../shared/models/apis/feedback-response.model';
import { Feedback } from '../../../../../shared/models/feedback.model';
import { ProductDetailService } from '../../product-detail.service';
import { ProductStarRatingService } from '../product-star-rating-panel/product-star-rating.service';

const FEEDBACK_API_URL = 'api/feedback';
const SIZE = 8;
@Injectable({
  providedIn: 'root'
})
export class ProductFeedbackService {
  private readonly authService = inject(AuthService);
  private readonly productDetailService = inject(ProductDetailService);
  private readonly productStarRatingService = inject(ProductStarRatingService);
  private readonly http = inject(HttpClient);

  sort: WritableSignal<string> = signal('updatedAt,desc');
  page: WritableSignal<number> = signal(0);

  userFeedback: WritableSignal<Feedback | null> = signal(null);
  feedbacks: WritableSignal<Feedback[]> = signal([]);
  areAllFeedbacksLoaded = computed(() => {
    if (this.page() >= this.totalPages() - 1) {
      return true;
    }
    return false;
  });

  totalPages: WritableSignal<number> = signal(1);
  totalElements: WritableSignal<number> = signal(0);

  submitFeedback(feedback: Feedback): Observable<Feedback> {
    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${this.authService.getToken()}`
    );
    return this.http
      .post<Feedback>(FEEDBACK_API_URL, feedback, {
        headers,
        context: new HttpContext().set(SkipLoading, true)
      })
      .pipe(
        tap(() => {
          this.initFeedbacks();
          this.findProductFeedbackOfUser().subscribe();
          this.productStarRatingService.fetchData();
        })
      );
  }

  private findProductFeedbacksByCriteria(
    productId: string = this.productDetailService.productId(),
    page: number = this.page(),
    sort: string = this.sort(),
    size: number = SIZE
  ): Observable<FeedbackApiResponse> {
    const requestParams = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    const requestURL = `${FEEDBACK_API_URL}/product/${productId}`;
    return this.http
      .get<FeedbackApiResponse>(requestURL, {
        params: requestParams,
        context: new HttpContext().set(SkipLoading, true)
      })
      .pipe(
        tap(response => {
          if (page === 0) {
            this.feedbacks.set(response._embedded.feedbacks);
          } else {
            this.feedbacks.set([
              ...this.feedbacks(),
              ...response._embedded.feedbacks
            ]);
          }
        })
      );
  }

  findProductFeedbackOfUser(
    productId: string = this.productDetailService.productId()
  ): Observable<Feedback> {
    const params = new HttpParams()
      .set('productId', productId)
      .set('userId', this.authService.getUserId() ?? '');
    const requestURL = FEEDBACK_API_URL;

    return this.http
      .get<Feedback>(requestURL, {
        params,
        context: new HttpContext().set(SkipLoading, true)
      })
      .pipe(
        tap(feedback => {
          this.userFeedback.set(feedback);
        }),
        catchError(() => {
          const feedback: Feedback = {
            content: '',
            rating: 0,
            productId
          };
          this.userFeedback.set(feedback);
          return of(feedback);
        })
      );
  }

  initFeedbacks(): void {
    this.page.set(0);
    this.findProductFeedbacksByCriteria().subscribe(response => {
      this.totalPages.set(response.page.totalPages);
      this.totalElements.set(response.page.totalElements);
    });
  }

  loadMoreFeedbacks(): void {
    this.page.update(value => value + 1);
    this.findProductFeedbacksByCriteria().subscribe();
  }

  changeSort(newSort: string): void {
    this.page.set(0);
    this.sort.set(newSort);
    this.findProductFeedbacksByCriteria().subscribe();
  }
}
