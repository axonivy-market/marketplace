import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProductStarRatingService } from './product-star-rating.service';
import { ProductDetailService } from '../../product-detail.service';
import { StarRatingCounting } from '../../../../../shared/models/star-rating-counting.model';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ProductStarRatingService', () => {
  let service: ProductStarRatingService;
  let httpMock: HttpTestingController;
  let productDetailService: jasmine.SpyObj<ProductDetailService>;

  const mockStarRatings: StarRatingCounting[] = [
    { starRating: 5, commentNumber: 10 },
    { starRating: 4, commentNumber: 5 },
    { starRating: 3, commentNumber: 2 }
  ];

  beforeEach(() => {
    const productDetailServiceSpy = jasmine.createSpyObj('ProductDetailService', ['productId']);

    TestBed.configureTestingModule({
      providers: [
        ProductStarRatingService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ProductDetailService, useValue: productDetailServiceSpy }
      ]
    });

    service = TestBed.inject(ProductStarRatingService);
    httpMock = TestBed.inject(HttpTestingController);
    productDetailService = TestBed.inject(ProductDetailService) as jasmine.SpyObj<ProductDetailService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch data and set star ratings', () => {
    const productId = '123';
    productDetailService.productId.and.returnValue(productId);

    service.fetchData();

    const req = httpMock.expectOne(`api/feedback/product/${productId}/rating`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStarRatings);

    expect(service.starRatings()).toEqual([
      { starRating: 5, commentNumber: 10 },
      { starRating: 4, commentNumber: 5 },
      { starRating: 3, commentNumber: 2 }
    ]);
  });

  it('should calculate total comments', () => {
    service.starRatings.set(mockStarRatings);
    expect(service.totalComments()).toBe(17);
  });

  it('should calculate review number', () => {
    service.starRatings.set(mockStarRatings);
    expect(service.reviewNumber()).toBe(4.5);
  });

  it('should sort star ratings by star rating', () => {
    const unsortedRatings = [
      { starRating: 3, commentNumber: 2 },
      { starRating: 5, commentNumber: 10 },
      { starRating: 4, commentNumber: 5 }
    ];

    service['sortByStar'](unsortedRatings);
    expect(unsortedRatings).toEqual([
      { starRating: 5, commentNumber: 10 },
      { starRating: 4, commentNumber: 5 },
      { starRating: 3, commentNumber: 2 }
    ]);
  });
});
