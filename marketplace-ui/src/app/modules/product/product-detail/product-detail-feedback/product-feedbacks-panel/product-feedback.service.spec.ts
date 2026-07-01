import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { ProductFeedbackService } from './product-feedback.service';
import { AuthService } from '../../../../../auth/auth.service';
import { ProductDetailService } from '../../product-detail.service';
import { ProductStarRatingService } from '../product-star-rating-panel/product-star-rating.service';
import { AdminAuthService } from '../../../../admin-dashboard/admin-auth.service';
import { FeedbackStatus } from '../../../../../shared/enums/feedback-status.enum';

describe('ProductFeedbackService', () => {
  let service: ProductFeedbackService;
  let httpMock: HttpTestingController;
  const productStarRatingService = {
    fetchData: vi.fn()
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductFeedbackService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: {
            getUserId: vi.fn().mockReturnValue('user-1'),
            getToken: vi.fn().mockReturnValue('token-1')
          }
        },
        {
          provide: ProductDetailService,
          useValue: {
            productId: signal('portal')
          }
        },
        {
          provide: ProductStarRatingService,
          useValue: productStarRatingService
        },
        {
          provide: AdminAuthService,
          useValue: {
            clearToken: vi.fn()
          }
        }
      ]
    });

    service = TestBed.inject(ProductFeedbackService);
    httpMock = TestBed.inject(HttpTestingController);
    productStarRatingService.fetchData.mockReset();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('submits feedback without authorization headers', () => {
    const feedback = {
      productId: 'portal',
      content: 'Looks good',
      rating: 5,
      feedbackStatus: FeedbackStatus.PENDING,
      moderatorName: '',
      productNames: {},
      version: 0
    } as any;
    vi.spyOn(service, 'fetchFeedbacks').mockImplementation(() => undefined);

    service.submitFeedback(feedback).subscribe();

    const request = httpMock.expectOne('api/feedback');
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.has('Authorization')).toBe(false);
    expect(request.request.headers.has('X-Authorization')).toBe(false);
    request.flush(feedback);

    expect(productStarRatingService.fetchData).toHaveBeenCalled();
  });

  it('loads approval feedbacks without authorization headers', () => {
    service.findProductFeedbacks().subscribe();

    const request = httpMock.expectOne('api/feedback/approval?page=0&size=40');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({
      _embedded: { feedbacks: [] },
      page: { totalPages: 1, totalElements: 0 }
    });
  });

  it('skips user feedback lookup when token is missing', () => {
    const authService = TestBed.inject(AuthService) as {
      getToken: ReturnType<typeof vi.fn>;
      getUserId: ReturnType<typeof vi.fn>;
    };
    authService.getToken.mockReturnValue(null);

    service.findProductFeedbackOfUser().subscribe(userFeedbacks => {
      expect(userFeedbacks).toEqual([]);
    });

    httpMock.expectNone('api/feedback?productId=portal&userId=user-1');
    expect(service.userFeedback()).toBeNull();
  });
});
