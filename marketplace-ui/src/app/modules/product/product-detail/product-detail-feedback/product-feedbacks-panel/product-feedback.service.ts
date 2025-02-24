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
import { catchError, Observable, of, tap, throwError } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from '../../../../../auth/auth.service';
import { ForwardingError } from '../../../../../core/interceptors/api.interceptor';
import { FeedbackApiResponse } from '../../../../../shared/models/apis/feedback-response.model';
import { Feedback } from '../../../../../shared/models/feedback.model';
import { ProductDetailService } from '../../product-detail.service';
import { ProductStarRatingService } from '../product-star-rating-panel/product-star-rating.service';
import {
  FEEDBACK_SORT_TYPES,
  NOT_FOUND_ERROR_CODE,
  TOKEN_KEY,
  USER_NOT_FOUND_ERROR_CODE
} from '../../../../../shared/constants/common.constant';
import { FeedbackStatus } from '../../../../../shared/enums/feedback-status.enum';
import { API_URI } from '../../../../../shared/constants/api.constant';

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
  private readonly cookieService = inject(CookieService);

  sort: WritableSignal<string> = signal(FEEDBACK_SORT_TYPES[0].value);
  page: WritableSignal<number> = signal(0);

  userFeedback: WritableSignal<Feedback | null> = signal(null);
  feedbacks: WritableSignal<Feedback[]> = signal([]);
  allFeedbacks: WritableSignal<Feedback[]> = signal([]);
  pendingFeedbacks: WritableSignal<Feedback[]> = signal([]);
  areAllFeedbacksLoaded = computed(() => {
    if (this.page() >= this.totalPages() - 1) {
      return true;
    }
    return false;
  });

  totalPages: WritableSignal<number> = signal(1);
  totalElements: WritableSignal<number> = signal(0);

  findProductFeedbacks(
    page: number = this.page(),
    sort: string = this.sort(),
    size: number = SIZE
  ): Observable<FeedbackApiResponse> {
    let requestParams = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    const requestURL = `${API_URI.FEEDBACK_APPROVAL}`;
    return this.http
      .get<FeedbackApiResponse>(requestURL, { params: requestParams })
      .pipe(
        tap(response => {
          if (page === 0) {
            this.allFeedbacks.set(response._embedded.feedbacks);
          } else {
            this.allFeedbacks.set([
              ...this.feedbacks(),
              ...response._embedded.feedbacks
            ]);
          }
          this.pendingFeedbacks.set(this.allFeedbacks().filter(f => f?.feedbackStatus === FeedbackStatus.PENDING));
        })
      );
  }

  updateFeedbackStatus(feedbackId: string, isApproved: boolean, moderatorName: string): Observable<Feedback> {
    const requestBody = {
      feedbackId,
      isApproved,
      moderatorName
    };
    const requestURL = `${API_URI.FEEDBACK_APPROVAL}`;

    return this.http.put<Feedback>(requestURL, requestBody).pipe(
      tap(updatedFeedback => {
        console.log("Feedback updated:", updatedFeedback);

        // Update local lists
        this.allFeedbacks.set(
          this.allFeedbacks().map(fb => fb.id === updatedFeedback.id ? updatedFeedback : fb)
        );

        // Recalculate pending & reviewed lists
        this.pendingFeedbacks.set(
          this.allFeedbacks().filter(fb => fb.feedbackStatus === FeedbackStatus.PENDING)
        );
      })
    );
  }

  submitFeedback(feedback: Feedback): Observable<Feedback> {
    const headers = new HttpHeaders().set(
      'X-Authorization',
      `Bearer ${this.authService.getToken()}`
    );
    return this.http
      .post<Feedback>(FEEDBACK_API_URL, feedback, {
        headers,
        context: new HttpContext().set(ForwardingError, true)
      })
      .pipe(
        tap(() => {
          this.fetchFeedbacks();
          // this.findProductFeedbackOfUser().subscribe();
          this.productStarRatingService.fetchData();
        }),
        catchError(response => {
          if (
            response.status === NOT_FOUND_ERROR_CODE &&
            response.error.helpCode === USER_NOT_FOUND_ERROR_CODE.toString()
          ) {
            this.clearTokenCookie();
          }
          return throwError(() => response);
        })
      );
  }

  findProductFeedbacksByCriteria(
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
      .get<FeedbackApiResponse>(requestURL, { params: requestParams })
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

          // if(this.userFeedback() && this.userFeedback()!.feedbackStatus === FeedbackStatus.PENDING) {
          //   // this.feedbacks().push(this.userFeedback()!);
          //   if (this.feedbacks().length > 0) {
          //     this.feedbacks().push(this.userFeedback()!);
          //   } else {
          //     this.feedbacks.set([this.userFeedback()!]);
          //   }
          //   // this.feedbacks.set(this.feedbacks().length > 0 ? [this.userFeedback(), ...this.feedbacks()] : [this.userFeedback()]);
          // }

//           const userFeedback = this.userFeedback();
// if (userFeedback && userFeedback.feedbackStatus === FeedbackStatus.PENDING) {
//   const currentFeedbacks = this.feedbacks(); // Get current feedbacks
//   this.feedbacks.set(currentFeedbacks.length ? [userFeedback, ...currentFeedbacks] : [userFeedback]);
// }

          // console.log(userFeedback);
          // console.log(this.feedbacks());
        })
      ).pipe(
        tap(response => {
          this.findProductFeedbackOfUser().subscribe();
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
        context: new HttpContext().set(ForwardingError, true)
      })
      .pipe(
        tap(feedback => {
          this.userFeedback.set(feedback);
          // const userFeedback = this.userFeedback();
          // if (userFeedback && userFeedback.feedbackStatus === FeedbackStatus.PENDING) {
          //   const currentFeedbacks = this.feedbacks(); // Get current feedbacks
          //   this.feedbacks.set(currentFeedbacks.length > 0 ? [userFeedback, ...currentFeedbacks] : [userFeedback]);
          // }

          if(this.userFeedback() && this.userFeedback()!.feedbackStatus === FeedbackStatus.PENDING) {
            // this.feedbacks().push(this.userFeedback()!);
            if (this.feedbacks().length > 0) {
              this.feedbacks().unshift(this.userFeedback()!);
            } else {
              this.feedbacks.set([this.userFeedback()!]);
            }
            // this.feedbacks.set(this.feedbacks().length > 0 ? [this.userFeedback(), ...this.feedbacks()] : [this.userFeedback()]);
          }
        }),
        catchError(response => {
          if (
            response.status === NOT_FOUND_ERROR_CODE &&
            response.error.helpCode === USER_NOT_FOUND_ERROR_CODE.toString()
          ) {
            this.clearTokenCookie();
          }
          const feedback: Feedback = {
            content: '',
            rating: 0,
            feedbackStatus: FeedbackStatus.PENDING,
            moderatorName: '',
            productId
          };
          this.userFeedback.set(feedback);
          return of(feedback);
        })
      );
  }

  fetchFeedbacks(): void {
    this.getInitFeedbacksObservable().subscribe(response => {
      this.handleFeedbackApiResponse(response);
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

  private clearTokenCookie(): void {
    this.cookieService.delete(TOKEN_KEY);
  }

  handleFeedbackApiResponse(response: FeedbackApiResponse): void {
    this.totalPages.set(response.page.totalPages);
    this.totalElements.set(response.page.totalElements);
  }

  getInitFeedbacksObservable(): Observable<FeedbackApiResponse> {
    this.page.set(0);
    return this.findProductFeedbacksByCriteria();
  }

  getInitAllFeedbacksObservable(): Observable<FeedbackApiResponse> {
    this.page.set(0);
    return this.findProductFeedbacks(); // Fetch all feedbacks
  }
}
