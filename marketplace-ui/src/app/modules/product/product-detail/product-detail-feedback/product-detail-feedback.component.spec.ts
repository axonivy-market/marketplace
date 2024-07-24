import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick
} from '@angular/core/testing';
import { ProductDetailFeedbackComponent } from './product-detail-feedback.component';
import { ProductStarRatingPanelComponent } from './product-star-rating-panel/product-star-rating-panel.component';
import { ShowFeedbacksDialogComponent } from './show-feedbacks-dialog/show-feedbacks-dialog.component';
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
        'initFeedbacks',
        'findProductFeedbackOfUser',
        'loadMoreFeedbacks',
        'areAllFeedbacksLoaded',
        'totalElements'
      ],
      {feedbacks: signal([] as Feedback[]), sort: signal('updatedAt,desc')}
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
});
