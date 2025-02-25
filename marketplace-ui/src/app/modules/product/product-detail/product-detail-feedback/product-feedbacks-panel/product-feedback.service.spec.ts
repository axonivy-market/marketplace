import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../../../../../auth/auth.service';
import { ProductDetailService } from '../../product-detail.service';
import { ProductStarRatingService } from '../product-star-rating-panel/product-star-rating.service';
import { ProductFeedbackService } from './product-feedback.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Feedback } from '../../../../../shared/models/feedback.model';
import { FeedbackStatus } from '../../../../../shared/enums/feedback-status.enum';

describe('ProductFeedbackService', () => {
  let service: ProductFeedbackService;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;
  let productStarRatingService: jasmine.SpyObj<ProductStarRatingService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken', 'getUserId']);
    const productDetailServiceSpy = jasmine.createSpyObj('ProductDetailService', ['productId']);
    const productStarRatingServiceSpy = jasmine.createSpyObj('ProductStarRatingService', ['fetchData']);

    TestBed.configureTestingModule({
      providers: [
        ProductFeedbackService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
        { provide: ProductStarRatingService, useValue: productStarRatingServiceSpy }
      ]
    });

    service = TestBed.inject(ProductFeedbackService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    productDetailService = TestBed.inject(ProductDetailService) as jasmine.SpyObj<ProductDetailService>;
    productStarRatingService = TestBed.inject(ProductStarRatingService) as jasmine.SpyObj<ProductStarRatingService>;
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
      reviewDate: new Date()
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

  it('should initialize feedbacks', () => {
    const mockResponse = {
      _embedded: { feedbacks: [{ content: 'Great product!', rating: 5, productId: '123', feedbackStatus: FeedbackStatus.PENDING, moderatorName: 'admin', reviewDate: new Date() }] },
      page: { totalPages: 2, totalElements: 5 }
    };

    productDetailService.productId.and.returnValue('123');

    service.fetchFeedbacks();
    const req = httpMock.expectOne( 'api/feedback/product/123?page=0&size=8&sort=newest' );
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(service.totalPages()).toBe(2);
    expect(service.totalElements()).toBe(5);
    expect(service.feedbacks()).toEqual([{ content: 'Great product!', rating: 5, productId: '123', feedbackStatus: FeedbackStatus.PENDING, moderatorName: 'admin', reviewDate: new Date() }]);
  });

  it('should load more feedbacks', () => {
    const initialFeedback: Feedback[] = [
      { content: 'Great product!', rating: 5, productId: '123', feedbackStatus: FeedbackStatus.APPROVED, moderatorName: 'admin', reviewDate: new Date() }
    ];
    const additionalFeedback: Feedback[] = [
      { content: 'Another review', rating: 4, productId: '123', feedbackStatus: FeedbackStatus.APPROVED, moderatorName: 'admin', reviewDate: new Date() }
    ];

    productDetailService.productId.and.returnValue('123');
    service.fetchFeedbacks();
    const initReq = httpMock.expectOne( 'api/feedback/product/123?page=0&size=8&sort=newest' );
    initReq.flush({ _embedded: { feedbacks: initialFeedback }, page: { totalPages: 2, totalElements: 5 } });

    service.loadMoreFeedbacks();
    const loadMoreReq = httpMock.expectOne( 'api/feedback/product/123?page=1&size=8&sort=newest' );
    loadMoreReq.flush({ _embedded: { feedbacks: additionalFeedback } });

    expect(service.feedbacks()).toEqual([...initialFeedback, ...additionalFeedback]);
  });

  it('should change sort and fetch feedbacks', () => {
    const mockDate = new Date();
    const mockResponse = {
      _embedded: { feedbacks: [{ content: 'Sorting test', rating: 3, productId: '123', feedbackStatus: FeedbackStatus.PENDING, moderatorName: 'admin', reviewDate: mockDate }] }
    };

    productDetailService.productId.and.returnValue('123');

    service.changeSort('rating,desc');
    const req = httpMock.expectOne('api/feedback/product/123?page=0&size=8&sort=rating,desc');
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(service.feedbacks()).toEqual([{ content: 'Sorting test', rating: 3, productId: '123', feedbackStatus: FeedbackStatus.PENDING, moderatorName: 'admin', reviewDate: mockDate }]);
  });

  it('should fetch feedbacks with token', () => {
    const mockDate = new Date();
    const mockResponse = {
      _embedded: { feedbacks: [{ content: 'Sorting test', rating: 3, productId: '123', feedbackStatus: FeedbackStatus.PENDING, moderatorName: 'admin', reviewDate: mockDate }] }
    };
    const token = 'mockToken';

    service.findProductFeedbacks(token).subscribe(response => {
      expect(service.allFeedbacks().length).toBe(1);
      expect(service.pendingFeedbacks().length).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('feedback/approval'));
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
      moderatorName: ''
    };

    const initialArray: Feedback[] = [initialFeedback];
    service.allFeedbacks.set(initialArray);
    
    const updatedFeedback: Feedback = { 
      id: '1',
      content: 'Test',
      rating: 0,
      feedbackStatus: FeedbackStatus.APPROVED,
      productId: '',
      moderatorName: 'admin'
    };
    
    service.updateFeedbackStatus('1', true, 'admin').subscribe(response => {
      expect(response).toEqual(updatedFeedback);
      expect(service.allFeedbacks()[0].feedbackStatus).toBe(FeedbackStatus.APPROVED);
      expect(service.pendingFeedbacks().length).toBe(0);
    });
  
    const req = httpMock.expectOne('api/feedback/approval');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedFeedback);
  });
});
