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
  let mockProductStarRatingService: jasmine.SpyObj<ProductStarRatingService>;
  let mockProductDetailService: jasmine.SpyObj<ProductDetailService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockProductStarRatingService = jasmine.createSpyObj('ProductStarRatingService', ['reviewNumber', 'totalComments']);
    mockProductDetailService = jasmine.createSpyObj('ProductDetailService', ['productId']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getToken', 'redirectToGitHub']);

    await TestBed.configureTestingModule({
      imports: [ProductStarRatingNumberComponent, StarRatingComponent, TranslateModule.forRoot()],
      providers: [
        { provide: ProductStarRatingService, useValue: mockProductStarRatingService },
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
    mockAuthService.getToken.and.returnValue('mockToken');
    spyOn(component.openAddFeedbackDialog, 'emit');
    const link = fixture.debugElement.query(By.css('.rate-link')).nativeElement;
    link.click();
    expect(component.openAddFeedbackDialog.emit).toHaveBeenCalled();
  });

  it('should redirect to GitHub if user is not authenticated', () => {
    mockAuthService.getToken.and.returnValue(null);
    mockProductDetailService.productId.and.returnValue('123');
    const link = fixture.debugElement.query(By.css('.rate-link')).nativeElement;
    link.click();
    expect(mockAuthService.redirectToGitHub).toHaveBeenCalledWith('123');
  });

  it('should render star rating and review number', () => {
    mockProductStarRatingService.reviewNumber.and.returnValue(4.5);
    mockProductStarRatingService.totalComments.and.returnValue(10);
    fixture.detectChanges();

    const reviewNumber = fixture.debugElement.query(By.css('.total-rating-number')).nativeElement;
    const totalComments = fixture.debugElement.query(By.css('h4.d-inline-block')).nativeElement;
    const starRatingComponent = fixture.debugElement.query(By.directive(StarRatingComponent));
    const reviewLabel = fixture.debugElement.query(By.css('.text-secondary.review-label-detail-page')).nativeElement;

    expect(reviewNumber.textContent).toContain('4.5');
    expect(totalComments.textContent).toContain('(10)');
    expect(reviewLabel.textContent).toContain('common.feedback.reviewLabel');
    expect(starRatingComponent).toBeTruthy();
  });
});
