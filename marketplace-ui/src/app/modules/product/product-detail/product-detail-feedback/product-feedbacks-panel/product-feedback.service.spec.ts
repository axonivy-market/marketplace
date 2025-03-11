import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../../../../../auth/auth.service';
import { ProductDetailService } from '../../product-detail.service';
import { ProductStarRatingService } from '../product-star-rating-panel/product-star-rating.service';
import { ProductFeedbackService } from './product-feedback.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Feedback } from '../../../../../shared/models/feedback.model';
import { FeedbackStatus } from '../../../../../shared/enums/feedback-status.enum';
import {
  NOT_FOUND_ERROR_CODE,
  USER_NOT_FOUND_ERROR_CODE
} from '../../../../../shared/constants/common.constant';
import { of } from 'rxjs';
import { FeedbackApiResponse } from '../../../../../shared/models/apis/feedback-response.model';

describe('ProductFeedbackService', () => {
  let service: ProductFeedbackService;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;
  let productStarRatingService: jasmine.SpyObj<ProductStarRatingService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getToken',
      'getUserId'
    ]);
    const productDetailServiceSpy = jasmine.createSpyObj(
      'ProductDetailService',
      ['productId']
    );
    const productStarRatingServiceSpy = jasmine.createSpyObj(
      'ProductStarRatingService',
      ['fetchData']
    );

    TestBed.configureTestingModule({
      providers: [
        ProductFeedbackService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
        {
          provide: ProductStarRatingService,
          useValue: productStarRatingServiceSpy
        }
      ]
    });

    service = TestBed.inject(ProductFeedbackService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    productDetailService = TestBed.inject(
      ProductDetailService
    ) as jasmine.SpyObj<ProductDetailService>;
    productStarRatingService = TestBed.inject(
      ProductStarRatingService
    ) as jasmine.SpyObj<ProductStarRatingService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should submit feedback successfully', () => {
    const feedback: Feedback = {
      content: 'Great product!',
      rating: 5,
      productId: '123',
      feedbackStatus: FeedbackStatus.APPROVED,
      moderatorName: 'admin',
      reviewDate: new Date(),
      version: 0
    };
    authService.getToken.and.returnValue('mockToken');

    service.submitFeedback(feedback).subscribe(result => {
      expect(result).toEqual(feedback);
    });

    const req = httpMock.expectOne('api/feedback');
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('X-Authorization')).toBe('Bearer mockToken');
    req.flush(feedback);
    expect(productStarRatingService.fetchData).toHaveBeenCalled();
  });

  it('should initialize feedbacks', done => {
    const mockResponse = {
      _embedded: {
        feedbacks: [
          {
            content: 'Great product!',
            rating: 5,
            productId: '123',
            feedbackStatus: FeedbackStatus.APPROVED,
            moderatorName: 'admin',
            version: 0
          }
        ]
      },
      page: { totalPages: 2, totalElements: 5 }
    };

    productDetailService.productId.and.returnValue('123');

    service.fetchFeedbacks();
    const req = httpMock.expectOne(
      'api/feedback/product/123?page=0&size=8&sort=newest'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    setTimeout(() => {
      expect(service.totalPages()).toBe(2);
      expect(service.totalElements()).toBe(5);
      expect(service.feedbacks()).toEqual(mockResponse._embedded.feedbacks);
      done();
    }, 0);
  });

  it('should load more feedbacks', () => {
    const initialFeedback: Feedback[] = [
      {
        content: 'Great product!',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: 'admin',
        reviewDate: new Date(),
        version: 0
      }
    ];
    const additionalFeedback: Feedback[] = [
      {
        content: 'Another review',
        rating: 4,
        productId: '123',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: 'admin',
        reviewDate: new Date(),
        version: 0
      }
    ];

    productDetailService.productId.and.returnValue('123');
    service.fetchFeedbacks();
    const initReq = httpMock.expectOne(
      'api/feedback/product/123?page=0&size=8&sort=newest'
    );
    initReq.flush({
      _embedded: { feedbacks: initialFeedback },
      page: { totalPages: 2, totalElements: 5 }
    });

    service.loadMoreFeedbacks();
    const loadMoreReq = httpMock.expectOne(
      'api/feedback/product/123?page=1&size=8&sort=newest'
    );
    loadMoreReq.flush({ _embedded: { feedbacks: additionalFeedback } });

    expect(service.feedbacks()).toEqual([
      ...initialFeedback,
      ...additionalFeedback
    ]);
  });

  it('should change sort and fetch feedbacks', done => {
    const mockDate = new Date();
    const mockResponse = {
      _embedded: {
        feedbacks: [
          {
            content: 'Sorting test',
            rating: 3,
            productId: '123',
            feedbackStatus: FeedbackStatus.APPROVED,
            moderatorName: 'admin',
            reviewDate: mockDate,
            version: 0
          }
        ]
      },
      page: { totalPages: 1, totalElements: 1 }
    };

    productDetailService.productId.and.returnValue('123');

    service.changeSort('rating,desc');
    const req = httpMock.expectOne(
      'api/feedback/product/123?page=0&size=8&sort=rating,desc'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    setTimeout(() => {
      expect(service.feedbacks()).toEqual(mockResponse._embedded.feedbacks);
      done();
    }, 0);
  });

  it('should fetch feedbacks with token', () => {
    const mockDate = new Date();
    const mockResponse = {
      _embedded: {
        feedbacks: [
          {
            content: 'Sorting test',
            rating: 3,
            productId: '123',
            feedbackStatus: FeedbackStatus.PENDING,
            moderatorName: 'admin',
            reviewDate: mockDate
          }
        ]
      },
      page: { totalPages: 1, totalElements: 1 }
    };
    const token = 'mockToken';

    spyOn(sessionStorage, 'getItem').and.returnValue(token);
    service.findProductFeedbacks().subscribe(() => {
      expect(service.allFeedbacks().length).toBe(0);
      expect(service.pendingFeedbacks().length).toBe(1);
    });

    const req = httpMock.expectOne('api/feedback/approval?page=0&size=40');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush(mockResponse);
  });

  it('should update feedback status and reflect in signals', () => {
    const initialFeedback: Feedback = {
      id: '1',
      content: 'Test',
      rating: 0,
      feedbackStatus: FeedbackStatus.PENDING,
      productId: '',
      moderatorName: '',
      version: 0
    };

    const initialArray: Feedback[] = [initialFeedback];
    service.allFeedbacks.set(initialArray);

    const updatedFeedback: Feedback = {
      id: '1',
      content: 'Test',
      rating: 0,
      feedbackStatus: FeedbackStatus.APPROVED,
      productId: '',
      moderatorName: 'admin',
      version: 1
    };

    service.updateFeedbackStatus('1', true, 'admin', 1).subscribe(response => {
      expect(response).toEqual(updatedFeedback);
      expect(service.allFeedbacks()[0].feedbackStatus).toBe(FeedbackStatus.APPROVED);
      expect(service.pendingFeedbacks().length).toBe(0);
    });

    const req = httpMock.expectOne('api/feedback/approval');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedFeedback);
  });

  it('should handle empty feedback response', done => {
    const mockResponse = {
      _embedded: { feedbacks: [] },
      page: { totalPages: 1, totalElements: 0 }
    };

    productDetailService.productId.and.returnValue('123');

    service.fetchFeedbacks();
    const req = httpMock.expectOne(
      'api/feedback/product/123?page=0&size=8&sort=newest'
    );
    req.flush(mockResponse);

    setTimeout(() => {
      expect(service.feedbacks().length).toBe(0);
      expect(service.totalPages()).toBe(1);
      expect(service.totalElements()).toBe(0);
      done();
    }, 0);
  });

  it('should sort feedbacks by date correctly', () => {
    const feedbacks: Feedback[] = [
      {
        id: '1',
        content: 'First',
        reviewDate: new Date('2023-01-02'),
        rating: 0,
        productId: '',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: '',
        version: 0
      },
      {
        id: '2',
        content: 'Second',
        reviewDate: new Date('2023-01-01'),
        rating: 0,
        productId: '',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: '',
        version: 0
      }
    ];

    const sorted = service['sortByDate'](feedbacks, 'reviewDate');
    expect(sorted[0].id).toBe('1');
    expect(sorted[1].id).toBe('2');
  });

  it('should fetch user-specific feedback successfully', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'User feedback',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.PENDING,
        moderatorName: '',
        userId: 'user1',
        version: 1
      }
    ];
    productDetailService.productId.and.returnValue('123');
    authService.getUserId.and.returnValue('user1');

    service.findProductFeedbackOfUser().subscribe(() => {
      expect(service.userFeedback()?.id).toBe('1');
      expect(service.userFeedback()?.feedbackStatus).toBe(
        FeedbackStatus.PENDING
      );
    });

    const req = httpMock.expectOne('api/feedback?productId=123&userId=user1');
    expect(req.request.method).toBe('GET');
    req.flush(mockFeedbacks);
  });

  it('should handle no user feedback found', () => {
    productDetailService.productId.and.returnValue('123');
    authService.getUserId.and.returnValue('user1');

    service.findProductFeedbackOfUser().subscribe(() => {
      expect(service.userFeedback()).toBeDefined();
      expect(service.userFeedback()?.feedbackStatus).toBe(
        FeedbackStatus.PENDING
      );
      expect(service.userFeedback()?.content).toBe('');
    });

    const req = httpMock.expectOne('api/feedback?productId=123&userId=user1');
    req.flush(
      { helpCode: USER_NOT_FOUND_ERROR_CODE.toString() },
      { status: NOT_FOUND_ERROR_CODE, statusText: 'Not Found' }
    );
  });

  it('should prioritize pending feedback over approved for user', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'Approved',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: '',
        userId: 'user1',
        version: 1
      },
      {
        id: '2',
        content: 'Pending',
        rating: 4,
        productId: '123',
        feedbackStatus: FeedbackStatus.PENDING,
        moderatorName: '',
        userId: 'user1',
        version: 0
      }
    ];
    productDetailService.productId.and.returnValue('123');
    authService.getUserId.and.returnValue('user1');

    service.findProductFeedbackOfUser().subscribe(() => {
      expect(service.userFeedback()?.id).toBe('2');
      expect(service.userFeedback()?.feedbackStatus).toBe(
        FeedbackStatus.PENDING
      );
    });

    const req = httpMock.expectOne('api/feedback?productId=123&userId=user1');
    req.flush(mockFeedbacks);
  });

  it('should correctly determine if all feedbacks are loaded', () => {
    service.page.set(0);
    service.totalPages.set(2);
    expect(service.areAllFeedbacksLoaded()).toBeFalse();

    service.page.set(1);
    expect(service.areAllFeedbacksLoaded()).toBeTrue();
  });

  it('should handle feedback approval API error', () => {
    const mockError = { status: 500, statusText: 'Server Error' };

    service.updateFeedbackStatus('1', true, 'admin', 1).subscribe({
      error: error => {
        expect(error.status).toBe(500);
      }
    });

    const req = httpMock.expectOne('api/feedback/approval');
    req.flush(null, mockError);
  });

  it('should filter out pending feedbacks from findProductFeedbacksByCriteria', () => {
    const mockResponse = {
      _embedded: {
        feedbacks: [
          {
            id: '1',
            content: 'Approved',
            rating: 5,
            productId: '123',
            feedbackStatus: FeedbackStatus.APPROVED,
            moderatorName: ''
          },
          {
            id: '2',
            content: 'Pending',
            rating: 4,
            productId: '123',
            feedbackStatus: FeedbackStatus.PENDING,
            moderatorName: ''
          }
        ]
      },
      page: { totalPages: 1, totalElements: 2 }
    };
    productDetailService.productId.and.returnValue('123');

    service.findProductFeedbacksByCriteria().subscribe(() => {
      expect(service.feedbacks().length).toBe(1);
      expect(service.feedbacks()[0].id).toBe('1');
    });

    const req = httpMock.expectOne(
      'api/feedback/product/123?page=0&size=8&sort=newest'
    );
    req.flush(mockResponse);
  });

  it('should not update feedbacks if userId does not match', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'User feedback',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.PENDING,
        moderatorName: '',
        userId: 'user2',
        version: 0
      }
    ];
    authService.getUserId.and.returnValue('user1');
    productDetailService.productId.and.returnValue('123');
    spyOn(service, 'findProductFeedbackOfUser').and.returnValue(of(mockFeedbacks));

    service['processUserFeedbacks']();
    httpMock.expectNone("api/feedback");
    expect(service.feedbacks()).toEqual([]);
  });

  it('should not update feedbacks if no pending feedbacks exist', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'Approved',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: '',
        userId: 'user1',
        version: 1
      }
    ];
    authService.getUserId.and.returnValue('user1');
    productDetailService.productId.and.returnValue('123');
    spyOn(service, 'findProductFeedbackOfUser').and.returnValue(of(mockFeedbacks));

    service['processUserFeedbacks']();
    httpMock.expectNone("api/feedback");
    expect(service.feedbacks()).toEqual([]);
  });

  it('should update feedbacks when user has both approved and pending feedbacks', () => {
    const approvedFeedback: Feedback = {
      id: '1',
      content: 'Approved',
      rating: 5,
      productId: '123',
      feedbackStatus: FeedbackStatus.APPROVED,
      moderatorName: '',
      userId: 'user1',
      version: 1
    };
    const pendingFeedback: Feedback = {
      id: '2',
      content: 'Pending',
      rating: 4,
      productId: '123',
      feedbackStatus: FeedbackStatus.PENDING,
      moderatorName: '',
      userId: 'user1',
      version: 0
    };
    service.feedbacks.set([approvedFeedback]);
    authService.getUserId.and.returnValue('user1');
    productDetailService.productId.and.returnValue('123');
    spyOn(service, 'findProductFeedbackOfUser').and.returnValue(of([pendingFeedback, approvedFeedback]));

    service['processUserFeedbacks']();
    expect(service.feedbacks()).toEqual([pendingFeedback]);
  });

  it('should handle invalid dates in sortByDate', () => {
    const feedbacks: Feedback[] = [
      { id: '1', content: 'First', reviewDate: null as any, rating: 0, productId: '', feedbackStatus: FeedbackStatus.APPROVED, moderatorName: '', version: 0 },
      { id: '2', content: 'Second', reviewDate: new Date('2023-01-01'), rating: 0, productId: '', feedbackStatus: FeedbackStatus.APPROVED, moderatorName: '', version: 0 }
    ];

    const sorted = service['sortByDate'](feedbacks, 'reviewDate');
    expect(sorted[0].id).toBe('2');
    expect(sorted[1].id).toBe('1');
  });

  it('should reset page to 0 in getInitFeedbacksObservable', () => {
    service.page.set(5);
    productDetailService.productId.and.returnValue('123');
    const mockResponse = { _embedded: { feedbacks: [] }, page: { totalPages: 1, totalElements: 0 } };

    service.getInitFeedbacksObservable().subscribe();
    const req = httpMock.expectOne('api/feedback/product/123?page=0&size=8&sort=newest');
    req.flush(mockResponse);

    expect(service.page()).toBe(0);
  });

  it('should not fetch more feedbacks if all are loaded', () => {
    service.page.set(1);
    service.totalPages.set(2);
    spyOn(service, 'findProductFeedbacksByCriteria').and.returnValue(of({} as FeedbackApiResponse));

    service.loadMoreFeedbacks();
    expect(service.findProductFeedbacksByCriteria).toHaveBeenCalled();
    expect(service.page()).toBe(2);
  });
});
