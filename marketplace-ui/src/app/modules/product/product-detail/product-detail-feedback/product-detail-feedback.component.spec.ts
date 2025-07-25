import {
  ComponentFixture,
  TestBed} from '@angular/core/testing';
import { ProductDetailFeedbackComponent } from './product-detail-feedback.component';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel/product-star-rating-panel.component';
import { ProductFeedbacksPanelComponent } from './product-feedbacks-panel/product-feedbacks-panel.component';
import { AppModalService } from '../../../../shared/services/app-modal.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductFeedbackService } from './product-feedbacks-panel/product-feedback.service';
import { AuthService } from '../../../../auth/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { ProductStarRatingService } from './product-star-rating-panel/product-star-rating.service';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { StarRatingCounting } from '../../../../shared/models/star-rating-counting.model';
import { signal } from '@angular/core';
import { Feedback } from '../../../../shared/models/feedback.model';

describe('ProductDetailFeedbackComponent', () => {
  let component: ProductDetailFeedbackComponent;
  let fixture: ComponentFixture<ProductDetailFeedbackComponent>;
  let mockAppModalService: jasmine.SpyObj<AppModalService>;
  let mockProductFeedbackService: jasmine.SpyObj<ProductFeedbackService>;
  let mockProductStarRatingService: jasmine.SpyObj<ProductStarRatingService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockActivatedRoute: any;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAppModalService = jasmine.createSpyObj('AppModalService', [
      'openAddFeedbackDialog',
      'openShowFeedbacksDialog'
    ]);
    mockProductFeedbackService = jasmine.createSpyObj(
      'ProductFeedbackService',
      [
        'fetchFeedbacks',
        'findProductFeedbackOfUser',
        'loadMoreFeedbacks',
        'areAllFeedbacksLoaded',
        'totalElements'
      ],
      { feedbacks: signal([] as Feedback[]), sort: signal('updatedAt,desc') }
    );
    mockProductStarRatingService = jasmine.createSpyObj(
      'ProductStarRatingService',
      ['fetchData'],
      { 
        reviewNumber: signal(0),
        totalComments: signal(0),
        starRatings: signal([] as StarRatingCounting[])
      }
    );
    mockAuthService = jasmine.createSpyObj('AuthService', ['getToken']);
    mockActivatedRoute = { queryParams: of({ showPopup: 'true' }) };
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ProductDetailFeedbackComponent, TranslateModule.forRoot()],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: AppModalService, useValue: mockAppModalService },
        {
          provide: ProductFeedbackService,
          useValue: mockProductFeedbackService
        },
        {
          provide: ProductStarRatingService,
          useValue: mockProductStarRatingService
        },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductDetailFeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render ProductStarRatingPanelComponent and ProductFeedbacksPanelComponent', () => {
    const starRatingPanel = fixture.debugElement.query(
      By.directive(ProductStarRatingPanelComponent)
    );
    const feedbacksPanel = fixture.debugElement.query(
      By.directive(ProductFeedbacksPanelComponent)
    );

    expect(starRatingPanel).toBeTruthy();
    expect(feedbacksPanel).toBeTruthy();
  });

  it('should call openShowFeedbacksDialog on button click if not in mobile mode', () => {
    spyOn(component, 'isMobileMode').and.returnValue(false);
    const button = fixture.debugElement.query(
      By.css('.btn-show-more')
    ).nativeElement;
    button.click();
    expect(mockAppModalService.openShowFeedbacksDialog).toHaveBeenCalled();
  });

  it('should call loadMoreFeedbacks on button click if in mobile mode', () => {
    spyOn(component, 'isMobileMode').and.returnValue(true);
    const button = fixture.debugElement.query(
      By.css('.btn-show-more')
    ).nativeElement;
    button.click();
    expect(mockProductFeedbackService.loadMoreFeedbacks).toHaveBeenCalled();
  });

  describe('isShowBtnMore computed signal', () => {
    it('should return false when all feedbacks are loaded and in mobile mode', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.and.returnValue(true);
      mockProductFeedbackService.totalElements.and.returnValue(10);
      
      // IMPORTANT: Recreate component AFTER setting mock return values
      fixture = TestBed.createComponent(ProductDetailFeedbackComponent);
      component = fixture.componentInstance;
      spyOn(component, 'isMobileMode').and.returnValue(true);

      fixture.detectChanges();
      expect(component.isShowBtnMore()).toBeFalse();
    });

    it('should return false when all feedbacks are loaded and totalElements <= MAX_ELEMENTS', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.and.returnValue(true);
      mockProductFeedbackService.totalElements.and.returnValue(3); // <= MAX_ELEMENTS

      fixture = TestBed.createComponent(ProductDetailFeedbackComponent);
      component = fixture.componentInstance;
      spyOn(component, 'isMobileMode').and.returnValue(false);

      fixture.detectChanges();
      expect(component.isShowBtnMore()).toBeFalse();
    });

    it('should return true when not all feedbacks are loaded', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.and.returnValue(false);
      mockProductFeedbackService.totalElements.and.returnValue(100);
      spyOn(component, 'isMobileMode').and.returnValue(false);

      expect(component.isShowBtnMore()).toBeTrue();
    });

    it('should return true when feedbacks are loaded but not in mobile mode and totalElements > MAX_ELEMENTS', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.and.returnValue(true);
      mockProductFeedbackService.totalElements.and.returnValue(10); // > MAX_ELEMENTS
      spyOn(component, 'isMobileMode').and.returnValue(false);

      expect(component.isShowBtnMore()).toBeTrue();
    });
});
});
