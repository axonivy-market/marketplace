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
  FEEDBACK_APPROVAL_SESSION_TOKEN,
  FEEDBACK_SORT_TYPES,
  NOT_FOUND_ERROR_CODE,
  TOKEN_KEY,
  USER_NOT_FOUND_ERROR_CODE
} from '../../../../../shared/constants/common.constant';
import { FeedbackStatus } from '../../../../../shared/enums/feedback-status.enum';
import { API_URI } from '../../../../../shared/constants/api.constant';

const FEEDBACK_API_URL = 'api/feedback';
const SIZE = 8;
const ALL_FEEDBACKS_SIZE = 40;
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
    size: number = ALL_FEEDBACKS_SIZE
  ): Observable<FeedbackApiResponse> {
    const token = sessionStorage.getItem(FEEDBACK_APPROVAL_SESSION_TOKEN);
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

          const sortedFeedbacks = this.sortByDate(feedbacks, 'reviewDate');

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
            this.sortByDate(this.pendingFeedbacks(), 'updatedAt')
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
    moderatorName: string,
    version: number
  ): Observable<Feedback> {
    const requestBody = { feedbackId, isApproved, moderatorName, version };
    const requestURL = `${API_URI.FEEDBACK_APPROVAL}`;

    return this.http.put<Feedback>(requestURL, requestBody).pipe(
      tap(updatedFeedback => {
        const updatedAllFeedbacks = this.allFeedbacks().map(feedback =>
          feedback.id === updatedFeedback.id ? updatedFeedback : feedback
        );

        this.allFeedbacks.set(
          this.sortByDate(updatedAllFeedbacks, 'updatedAt')
        );

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
          const approvedFeedbacks = (
            response._embedded?.feedbacks || []
          ).filter(
            f =>
              f.feedbackStatus === FeedbackStatus.APPROVED || !f.feedbackStatus
          );
          if (page === 0) {
            this.feedbacks.set(approvedFeedbacks);
          } else {
            this.feedbacks.set([...this.feedbacks(), ...approvedFeedbacks]);
          }
        }),
        tap(() => {
          this.findProductFeedbackOfUser().subscribe(userFeedbacks => {
            if (userFeedbacks?.length) {
              const feedback = userFeedbacks[0];

              if (feedback.userId === this.authService.getUserId()) {
                const pendingFeedbacks = userFeedbacks.filter(
                  f => f.feedbackStatus === FeedbackStatus.PENDING
                );
                const approvedExists = this.feedbacks().some(
                  f =>
                    f.userId === feedback.userId &&
                    (f.feedbackStatus === FeedbackStatus.APPROVED ||
                      !f.feedbackStatus)
                );

                if (pendingFeedbacks.length) {
                  if (approvedExists && userFeedbacks.length === 2) {
                    this.feedbacks.set([
                      pendingFeedbacks[0],
                      ...this.feedbacks().filter(
                        f => f.userId !== feedback.userId
                      )
                    ]);
                  } else {
                    this.feedbacks.set([
                      ...pendingFeedbacks,
                      ...this.feedbacks()
                    ]);
                  }
                }
              }
            }
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
          const prioritizedUserFeedback =
            feedbacks.find(f => f.feedbackStatus === FeedbackStatus.PENDING) ||
            feedbacks.find(f => f.feedbackStatus === FeedbackStatus.APPROVED) ||
            feedbacks.find(f => !f.feedbackStatus) ||
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

  private sortByDate<T>(items: T[], dateKey: keyof T): T[] {
    return [...items].sort((a, b) => {
      const dateA = new Date((a[dateKey] ?? 0) as any).getTime();
      const dateB = new Date((b[dateKey] ?? 0) as any).getTime();
      return dateB - dateA;
    });
  }
}
