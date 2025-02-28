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
import { catchError, map, Observable, of, switchMap, tap, throwError } from 'rxjs';
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
    size: number = 20
  ): Observable<FeedbackApiResponse> {
    const token = this.authService.decodeToken(
      this.cookieService.get(TOKEN_KEY)
    )?.accessToken;
    console.log(token);
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    const requestParams = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http
      .get<FeedbackApiResponse>(`${API_URI.FEEDBACK_APPROVAL}`, {
        headers,
        params: requestParams
      })
      .pipe(
        tap(response => {
          const feedbacks = response._embedded?.feedbacks || [];

          const sortedFeedbacks = feedbacks.sort(
            (a, b) =>
              (b.reviewDate ? new Date(b.reviewDate).getTime() : 0) -
              (a.reviewDate ? new Date(a.reviewDate).getTime() : 0)
          );

          const nonPendingFeedbacks = sortedFeedbacks.filter(
            f => f?.feedbackStatus !== FeedbackStatus.PENDING
          );
          const pendingFeedbacks = sortedFeedbacks.filter(
            f => f?.feedbackStatus === FeedbackStatus.PENDING
          );

          if (page === 0) {
            this.allFeedbacks.set(nonPendingFeedbacks);
            this.pendingFeedbacks.set(pendingFeedbacks);
          } else {
            this.allFeedbacks.set([
              ...this.allFeedbacks(),
              ...nonPendingFeedbacks
            ]);
            this.pendingFeedbacks.set([
              ...this.pendingFeedbacks(),
              ...pendingFeedbacks
            ]);
          }

          this.pendingFeedbacks.set(
            this.pendingFeedbacks().sort(
              (a, b) =>
                (b.updatedAt ? new Date(b.updatedAt).getTime() : 0) -
                (a.updatedAt ? new Date(a.updatedAt).getTime() : 0)
            )
          );
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

  updateFeedbackStatus(
    feedbackId: string,
    isApproved: boolean,
    moderatorName: string
  ): Observable<Feedback> {
    const requestBody = { feedbackId, isApproved, moderatorName };
    const requestURL = `${API_URI.FEEDBACK_APPROVAL}`;

    return this.http.put<Feedback>(requestURL, requestBody).pipe(
      tap(updatedFeedback => {
        const updatedAllFeedbacks = this.allFeedbacks()
          .map(feedback =>
            feedback.id === updatedFeedback.id ? updatedFeedback : feedback
          )
          .sort(
            (a, b) =>
              (b.updatedAt ? new Date(b.updatedAt).getTime() : 0) -
              (a.updatedAt ? new Date(a.updatedAt).getTime() : 0)
          );
        this.allFeedbacks.set([...updatedAllFeedbacks]);

        const filteredPendingFeedbacks = updatedAllFeedbacks.filter(
          feedback => feedback.feedbackStatus === FeedbackStatus.PENDING
        );
        this.pendingFeedbacks.set([...filteredPendingFeedbacks]);
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
          const approvedFeedbacks = (response._embedded?.feedbacks || []).filter(f => f.feedbackStatus === FeedbackStatus.APPROVED);
          console.log(approvedFeedbacks);
          if (page === 0) {
            this.feedbacks.set(approvedFeedbacks);
          } else {
            this.feedbacks.set([
              ...this.feedbacks(),
              ...approvedFeedbacks
            ]);
          }
        }),
        tap(response => {
          this.findProductFeedbackOfUser().subscribe(userFeedbacks => {
            if (userFeedbacks && userFeedbacks.length > 0) {
              console.log(userFeedbacks);
              const feedback = userFeedbacks[0];

              if (feedback.userId === this.authService.getUserId()) {
                if (userFeedbacks.find(f => f.feedbackStatus === FeedbackStatus.PENDING)) {
                  const currentFeedbacks = this.feedbacks();
                  const hasApprovedFeedback = currentFeedbacks.some(f =>
                    f.userId === feedback.userId &&
                    f.feedbackStatus === FeedbackStatus.APPROVED
                  );
                  console.log(hasApprovedFeedback);
                  if (hasApprovedFeedback && userFeedbacks.length === 2) {
                    // Case 1: User has both approved and pending, replace approved with pending
                    const pending = userFeedbacks.filter(f => f.feedbackStatus === FeedbackStatus.PENDING)[0];
                    this.feedbacks.set([
                      pending,
                      ...currentFeedbacks.filter(f => f.userId !== feedback.userId)
                    ]);
                    console.log(feedback);
                    console.log(pending);
                    console.log(this.feedbacks());
                  } else if (userFeedbacks.length === 2 && currentFeedbacks.length === 0) {
                    console.log("here");
                    console.log(feedback);
                    // Case 2: User has both approved and pending but current feedbacks empty, add pending
                    this.feedbacks.set([feedback]);
                  } else {
                    // Case 3: User has only one pending feedback
                    this.feedbacks.set([feedback, ...currentFeedbacks]);
                  }
                }
              }
            }
            console.log(this.feedbacks());
          });
        })
      );
  }

  findProductFeedbackOfUser(
    productId: string = this.productDetailService.productId()
  ): Observable<Feedback[]> {
    const params = new HttpParams()
      .set('productId', productId)
      .set('userId', this.authService.getUserId() ?? '');
    const requestURL = FEEDBACK_API_URL;
    return this.http
      .get<Feedback[]>(requestURL, {
        params,
        context: new HttpContext().set(ForwardingError, true)
      })
      .pipe(
        tap(feedbacks => {
          const prioritizedUserFeedback = feedbacks.find(f => f.feedbackStatus === FeedbackStatus.PENDING) ||
            feedbacks.find(f => f.feedbackStatus === FeedbackStatus.APPROVED) ||
            null;
          this.userFeedback.set(prioritizedUserFeedback);
        }),
        catchError(response => {
          if (
            response.status === NOT_FOUND_ERROR_CODE &&
            response.error.helpCode === USER_NOT_FOUND_ERROR_CODE.toString()
          ) {
            this.clearTokenCookie();
          }
          const defaultFeedback: Feedback = {
            content: '',
            rating: 0,
            feedbackStatus: FeedbackStatus.PENDING,
            moderatorName: '',
            productId
          };
          this.userFeedback.set(defaultFeedback);
          return of([defaultFeedback]);
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
}
