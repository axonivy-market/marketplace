import type { MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
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
  let mockAppModalService: MockedObject<AppModalService>;
  let mockProductFeedbackService: MockedObject<ProductFeedbackService>;
  let mockProductStarRatingService: MockedObject<ProductStarRatingService>;
  let mockAuthService: MockedObject<AuthService>;
  let mockActivatedRoute: any;
  let mockRouter: MockedObject<Router>;

  beforeEach(async () => {
    mockAppModalService = {
      openAddFeedbackDialog: vi
        .fn()
        .mockName('AppModalService.openAddFeedbackDialog'),
      openShowFeedbacksDialog: vi
        .fn()
        .mockName('AppModalService.openShowFeedbacksDialog')
    };
    mockProductFeedbackService = {
      fetchFeedbacks: vi.fn().mockName('ProductFeedbackService.fetchFeedbacks'),
      findProductFeedbackOfUser: vi
        .fn()
        .mockName('ProductFeedbackService.findProductFeedbackOfUser'),
      loadMoreFeedbacks: vi
        .fn()
        .mockName('ProductFeedbackService.loadMoreFeedbacks'),
      areAllFeedbacksLoaded: vi
        .fn()
        .mockName('ProductFeedbackService.areAllFeedbacksLoaded'),
      totalElements: vi.fn().mockName('ProductFeedbackService.totalElements'),
      feedbacks: signal([] as Feedback[]),
      sort: signal('updatedAt,desc')
    };
    mockProductStarRatingService = {
      fetchData: vi.fn().mockName('ProductStarRatingService.fetchData'),
      reviewNumber: signal(0),
      totalComments: signal(0),
      starRatings: signal([] as StarRatingCounting[])
    };
    mockAuthService = {
      getToken: vi.fn().mockName('AuthService.getToken')
    };
    mockActivatedRoute = { queryParams: of({ showPopup: 'true' }) };
    mockRouter = {
      navigate: vi.fn().mockName('Router.navigate')
    };

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
    vi.spyOn(component, 'isMobileMode').mockReturnValue(false);
    const button = fixture.debugElement.query(
      By.css('.btn-show-more')
    ).nativeElement;
    button.click();
    expect(mockAppModalService.openShowFeedbacksDialog).toHaveBeenCalled();
  });

  it('should call loadMoreFeedbacks on button click if in mobile mode', () => {
    vi.spyOn(component, 'isMobileMode').mockReturnValue(true);
    const button = fixture.debugElement.query(
      By.css('.btn-show-more')
    ).nativeElement;
    button.click();
    expect(mockProductFeedbackService.loadMoreFeedbacks).toHaveBeenCalled();
  });

  describe('isShowBtnMore computed signal', () => {
    it('should return false when all feedbacks are loaded and in mobile mode', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.mockReturnValue(true);
      mockProductFeedbackService.totalElements.mockReturnValue(10);

      // IMPORTANT: Recreate component AFTER setting mock return values
      fixture = TestBed.createComponent(ProductDetailFeedbackComponent);
      component = fixture.componentInstance;
      vi.spyOn(component, 'isMobileMode').mockReturnValue(true);

      fixture.detectChanges();
      expect(component.isShowBtnMore()).toBe(false);
    });

    it('should return false when all feedbacks are loaded and totalElements <= MAX_ELEMENTS', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.mockReturnValue(true);
      mockProductFeedbackService.totalElements.mockReturnValue(3); // <= MAX_ELEMENTS

      fixture = TestBed.createComponent(ProductDetailFeedbackComponent);
      component = fixture.componentInstance;
      vi.spyOn(component, 'isMobileMode').mockReturnValue(false);

      fixture.detectChanges();
      expect(component.isShowBtnMore()).toBe(false);
    });

    it('should return true when not all feedbacks are loaded', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.mockReturnValue(false);
      mockProductFeedbackService.totalElements.mockReturnValue(100);
      vi.spyOn(component, 'isMobileMode').mockReturnValue(false);

      expect(component.isShowBtnMore()).toBe(true);
    });

    it('should return true when feedbacks are loaded but not in mobile mode and totalElements > MAX_ELEMENTS', () => {
      mockProductFeedbackService.areAllFeedbacksLoaded.mockReturnValue(true);
      mockProductFeedbackService.totalElements.mockReturnValue(10); // > MAX_ELEMENTS
      vi.spyOn(component, 'isMobileMode').mockReturnValue(false);

      expect(component.isShowBtnMore()).toBe(true);
    });
  });
});
