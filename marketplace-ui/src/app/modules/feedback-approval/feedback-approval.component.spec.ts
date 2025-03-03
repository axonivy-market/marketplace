import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { FeedbackApprovalComponent } from './feedback-approval.component';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../auth/auth.service';
import { AppModalService } from '../../shared/services/app-modal.service';
import { ProductFeedbackService } from '../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';
import { signal } from '@angular/core';

describe('FeedbackApprovalComponent', () => {
  let component: FeedbackApprovalComponent;
  let fixture: ComponentFixture<FeedbackApprovalComponent>;
  let authServiceMock: any;
  let productFeedbackServiceMock: any;
  let languageService: jasmine.SpyObj<LanguageService>;

  beforeEach(async () => {
    authServiceMock = {
      getToken: jasmine.createSpy().and.returnValue('mockToken'),
      redirectToGitHub: jasmine.createSpy(),
      getDisplayName: jasmine.createSpy().and.returnValue('TestUser')
    };

    productFeedbackServiceMock = {
      allFeedbacks: signal([]),
      pendingFeedbacks: signal([]),
      findProductFeedbacks: jasmine.createSpy().and.returnValue(of([])),
      updateFeedbackStatus: jasmine.createSpy().and.returnValue(of(null))
    };

    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'selectedLanguage'
    ]);

    await TestBed.configureTestingModule({
      imports: [FeedbackApprovalComponent, TranslateModule.forRoot()],
      providers: [
        ThemeService,
        { provide: AuthService, useValue: authServiceMock },
        { provide: AppModalService, useValue: {} },
        {
          provide: ProductFeedbackService,
          useValue: productFeedbackServiceMock
        },
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;
    fixture = TestBed.createComponent(FeedbackApprovalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call fetchFeedbacks on ngOnInit if token exists', () => {
    spyOn(component, 'fetchFeedbacks');
    component.ngOnInit();
    expect(component.fetchFeedbacks).toHaveBeenCalled();
  });

  it('should call findProductFeedbacks on fetchFeedbacks and set isAuthenticated', () => {
    component.fetchFeedbacks();
    expect(productFeedbackServiceMock.findProductFeedbacks).toHaveBeenCalled();
    expect(component.isAuthenticated).toBeTrue();
  });

  it('should set isAuthenticated to false on fetchFeedbacks error', () => {
    productFeedbackServiceMock.findProductFeedbacks.and.returnValue(
      throwError(() => new Error('Error'))
    );
    component.fetchFeedbacks();
    expect(component.isAuthenticated).toBeFalse();
  });

  it('should call updateFeedbackStatus on onClickReviewButton', () => {
    const mockFeedback = { id: 1 } as any;
    component.onClickReviewButton(mockFeedback, true);
    expect(
      productFeedbackServiceMock.updateFeedbackStatus
    ).toHaveBeenCalledWith(1, true, 'TestUser', 1);
  });

  it('should update activeTab when setActiveTab is called', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
  });

  it('should render review and history tabs', () => {
    fixture.detectChanges();
    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));

    expect(reviewTab.nativeElement.textContent.trim()).toContain(
      'common.approval.reviewFeedback'
    );
    expect(historyTab.nativeElement.textContent.trim()).toContain(
      'common.approval.history'
    );
  });

  it('should change activeTab when clicking tabs', () => {
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));
    historyTab.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.activeTab).toBe('history');
  });

  it('should pass correct feedbacks to FeedbackTableComponent', () => {
    const mockPendingFeedbacks = [{ id: 1, content: 'Great product!' }];
    const mockFeedbacks = [{ id: 2, content: 'Awesome service!' }];

    productFeedbackServiceMock.pendingFeedbacks.set(mockPendingFeedbacks);
    productFeedbackServiceMock.allFeedbacks.set(mockFeedbacks);
    fixture.detectChanges();

    const reviewTable = fixture.debugElement.queryAll(
      By.directive(FeedbackTableComponent)
    )[0];
    const historyTable = fixture.debugElement.queryAll(
      By.directive(FeedbackTableComponent)
    )[1];

    expect(reviewTable.componentInstance.feedbacks).toEqual(
      mockPendingFeedbacks
    );
    expect(historyTable.componentInstance.feedbacks).toEqual(mockFeedbacks);
  });

  it('should trigger onClickReviewButton when review button is clicked', () => {
    spyOn(component, 'onClickReviewButton');

    const mockFeedback = { id: 1 } as any;
    productFeedbackServiceMock.pendingFeedbacks.set([mockFeedback]);
    fixture.detectChanges();

    const approveButton = fixture.debugElement.query(By.css('#approve-button'));
    approveButton.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.onClickReviewButton).toHaveBeenCalledWith(
      mockFeedback,
      true
    );
  });

  it('should switch to history tab when clicked', fakeAsync(() => {
    component.isAuthenticated = true;
    fixture.detectChanges();

    const historyTab = fixture.debugElement.query(By.css('#history-tab'));
    historyTab.triggerEventHandler('click', null);
    fixture.detectChanges();
    tick();

    expect(component.activeTab).toBe('history');
    expect(historyTab.classes['active']).toBeTrue();

    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    expect(reviewTab.classes['active']).toBeUndefined();

    const reviewPane = fixture.debugElement.query(
      By.css('.tab-pane[aria-labelledby="review-tab"]')
    );
    const historyPane = fixture.debugElement.query(
      By.css('.tab-pane[aria-labelledby="history-tab"]')
    );
    expect(reviewPane.classes['active']).toBeUndefined();
    expect(historyPane.classes['active']).toBeTrue();
  }));

  it('should pass pending feedbacks to review tab’s FeedbackTableComponent', () => {
    component.isAuthenticated = true;
    const mockPendingFeedbacks = [{ id: 1, content: 'Great product!' }];
    productFeedbackServiceMock.pendingFeedbacks.set(mockPendingFeedbacks);
    fixture.detectChanges();

    const feedbackTables = fixture.debugElement.queryAll(
      By.directive(FeedbackTableComponent)
    );
    const reviewTable = feedbackTables[0];

    expect(reviewTable.componentInstance.feedbacks).toEqual(
      mockPendingFeedbacks
    );
    expect(reviewTable.componentInstance.isHistoryTab).toBeFalse();
  });

  it('should pass all feedbacks to history tab’s FeedbackTableComponent', () => {
    component.isAuthenticated = true;
    const mockAllFeedbacks = [{ id: 2, content: 'Awesome service!' }];
    productFeedbackServiceMock.allFeedbacks.set(mockAllFeedbacks);
    fixture.detectChanges();

    const feedbackTables = fixture.debugElement.queryAll(
      By.directive(FeedbackTableComponent)
    );
    const historyTable = feedbackTables[1];

    expect(historyTable.componentInstance.feedbacks).toEqual(mockAllFeedbacks);
    expect(historyTable.componentInstance.isHistoryTab).toBeTrue();
  });
});