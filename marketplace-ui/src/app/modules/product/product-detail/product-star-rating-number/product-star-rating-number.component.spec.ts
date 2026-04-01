import type { MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductStarRatingNumberComponent } from './product-star-rating-number.component';
import { TranslateModule } from '@ngx-translate/core';
import { StarRatingComponent } from '../../../../shared/components/star-rating/star-rating.component';
import { ProductStarRatingService } from '../product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { AuthService } from '../../../../auth/auth.service';
import { ProductDetailService } from '../product-detail.service';
import { By } from '@angular/platform-browser';

describe('ProductStarRatingNumberComponent', () => {
  let component: ProductStarRatingNumberComponent;
  let fixture: ComponentFixture<ProductStarRatingNumberComponent>;
  let mockProductStarRatingService: MockedObject<ProductStarRatingService>;
  let mockProductDetailService: MockedObject<ProductDetailService>;
  let mockAuthService: MockedObject<AuthService>;

  beforeEach(async () => {
    mockProductStarRatingService = {
      reviewNumber: vi.fn().mockName('ProductStarRatingService.reviewNumber'),
      totalComments: vi.fn().mockName('ProductStarRatingService.totalComments')
    };
    mockProductDetailService = {
      productId: vi.fn().mockName('ProductDetailService.productId')
    };
    mockAuthService = {
      getToken: vi.fn().mockName('AuthService.getToken'),
      redirectToGitHub: vi.fn().mockName('AuthService.redirectToGitHub')
    };

    await TestBed.configureTestingModule({
      imports: [
        ProductStarRatingNumberComponent,
        StarRatingComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        {
          provide: ProductStarRatingService,
          useValue: mockProductStarRatingService
        },
        { provide: ProductDetailService, useValue: mockProductDetailService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductStarRatingNumberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should inject services', () => {
    expect(component['productStarRatingService']).toBeDefined();
    expect(component['productDetailService']).toBeDefined();
    expect(component['authService']).toBeDefined();
  });

  it('should emit openAddFeedbackDialog event if user is authenticated', () => {
    mockAuthService.getToken.mockReturnValue('mockToken');
    vi.spyOn(component.openAddFeedbackDialog, 'emit');
    const link = fixture.debugElement.query(By.css('.rate-link')).nativeElement;
    link.click();
    expect(component.openAddFeedbackDialog.emit).toHaveBeenCalled();
  });

  it('should redirect to GitHub if user is not authenticated', () => {
    mockAuthService.getToken.mockReturnValue(null);
    mockProductDetailService.productId.mockReturnValue('123');
    const link = fixture.debugElement.query(By.css('.rate-link')).nativeElement;
    link.click();
    expect(mockAuthService.redirectToGitHub).toHaveBeenCalledWith('123');
  });

  it('should render star rating and review number', () => {
    mockProductStarRatingService.reviewNumber.mockReturnValue(4.5);
    mockProductStarRatingService.totalComments.mockReturnValue(10);
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();

    const reviewNumber = fixture.debugElement.query(
      By.css('.total-rating-number')
    )?.nativeElement;
    const totalComments = fixture.debugElement.query(
      By.css('h4.d-inline-block')
    )?.nativeElement;
    const starRatingComponent = fixture.debugElement.query(
      By.directive(StarRatingComponent)
    );
    const reviewLabel = fixture.debugElement.query(
      By.css('.text-secondary.review-label-detail-page')
    )?.nativeElement;

    expect(reviewLabel?.textContent).toContain('common.feedback.reviewLabel');
    expect(starRatingComponent).toBeTruthy();
    if (reviewNumber) {
      expect(reviewNumber.textContent).toContain('4.5');
    }
    if (totalComments) {
      expect(totalComments.textContent).toContain('(10)');
    }
  });
});
