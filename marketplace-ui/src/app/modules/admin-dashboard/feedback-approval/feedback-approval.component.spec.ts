import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { FeedbackApprovalComponent } from './feedback-approval.component';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../auth/auth.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { ProductFeedbackService } from '../../product/product-detail/product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';
import { signal } from '@angular/core';
import { ERROR_MESSAGES } from '../../../shared/constants/common.constant';
import { Feedback } from '../../../shared/models/feedback.model';
import { HttpErrorResponse } from '@angular/common/http';
import { FeedbackStatus } from '../../../shared/enums/feedback-status.enum';
import { MOCK_APPROVED_FEEDBACK } from '../../../shared/mocks/mock-data';

describe('FeedbackApprovalComponent', () => {
  let component: FeedbackApprovalComponent;
  let fixture: ComponentFixture<FeedbackApprovalComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let productFeedbackServiceMock: jasmine.SpyObj<ProductFeedbackService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', [
      'getToken',
      'redirectToGitHub',
      'getDisplayName',
      'getUserInfo',
      'getDisplayNameFromAccessToken'
    ]);
    authSpy.getDisplayName.and.returnValue('TestUser');
    authSpy.getDisplayNameFromAccessToken.and.returnValue(of('TestUser'));
    authSpy.getUserInfo.and.returnValue(of({ name: 'TestUser' }));

    const productFeedbackSpy = jasmine.createSpyObj('ProductFeedbackService', [
      'findProductFeedbacks',
      'updateFeedbackStatus'
    ]);
    productFeedbackSpy.allFeedbacks = signal([]);
    productFeedbackSpy.pendingFeedbacks = signal([]);
    productFeedbackSpy.findProductFeedbacks.and.returnValue(of([]));
    productFeedbackSpy.updateFeedbackStatus.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      imports: [FeedbackApprovalComponent, TranslateModule.forRoot()],
      providers: [
        ThemeService,
        { provide: AuthService, useValue: authSpy },
        { provide: AppModalService, useValue: {} },
        { provide: ProductFeedbackService, useValue: productFeedbackSpy },
        {
          provide: LanguageService,
          useValue: jasmine.createSpyObj('LanguageService', [
            'selectedLanguage'
          ])
        },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedbackApprovalComponent);
    component = fixture.componentInstance;
    authServiceMock = TestBed.inject(
      AuthService
    ) as jasmine.SpyObj<AuthService>;
    productFeedbackServiceMock = TestBed.inject(
      ProductFeedbackService
    ) as jasmine.SpyObj<ProductFeedbackService>;
    fixture.detectChanges();

    // Mock sessionStorage
    spyOn(sessionStorage, 'getItem').and.returnValue(null);
    spyOn(sessionStorage, 'setItem');
    spyOn(sessionStorage, 'removeItem');
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should update feedback status and refresh', fakeAsync(() => {
    const feedback: Feedback = {
      id: '1',
      version: 2,
      content: 'Test feedback',
      rating: 5,
      productId: '123',
      feedbackStatus: FeedbackStatus.PENDING,
      moderatorName: '',
      productNames: {},
      userId: '7c419872-4acf-48e9-b2e5-c6b268d21f53'
    };

    const updatedFeedback: Feedback = {
      ...feedback,
      feedbackStatus: FeedbackStatus.APPROVED
    };

    productFeedbackServiceMock.updateFeedbackStatus.and.returnValue(
      of(updatedFeedback)
    );

    component.moderatorName = 'TestUser';
    spyOn(component, 'fetchFeedbacks');
    component.onClickReviewButton(feedback, true);
    tick();

    expect(
      productFeedbackServiceMock.updateFeedbackStatus
    ).toHaveBeenCalledWith(MOCK_APPROVED_FEEDBACK);
    expect(component.fetchFeedbacks).toHaveBeenCalled();
  }));

  it('should update activeTab', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
  });

  it('should update activeTab when setActiveTab is called', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
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
    const mockPendingFeedbacks = [{ id: 1, content: 'Great product!' }] as any;
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
    const mockAllFeedbacks = [{ id: 2, content: 'Awesome service!' }] as any;
    productFeedbackServiceMock.allFeedbacks.set(mockAllFeedbacks);
    fixture.detectChanges();

    const feedbackTables = fixture.debugElement.queryAll(
      By.directive(FeedbackTableComponent)
    );
    const historyTable = feedbackTables[1];

    expect(historyTable.componentInstance.feedbacks).toEqual(mockAllFeedbacks);
    expect(historyTable.componentInstance.isHistoryTab).toBeTrue();
  });

  it('should initialize with stored token', () => {
    spyOn(component, 'fetchFeedbacks');
    (sessionStorage.getItem as jasmine.Spy).and.returnValue('storedToken');
    component.ngOnInit();
    expect(component.token).toBe('storedToken');
    expect(component.isAuthenticated).toBeTrue();
    expect(component.fetchFeedbacks).toHaveBeenCalled();
  });

  it('should handle fetchUserInfo success', fakeAsync(() => {
    component.token = 'testToken';
    component.fetchUserInfo();
    tick();
    expect(authServiceMock.getUserInfo).toHaveBeenCalledWith('testToken');
    expect(authServiceMock.getDisplayNameFromAccessToken).toHaveBeenCalledWith(
      'testToken'
    );
  }));

  it('should handle non-unauthorized errors in fetchFeedbacks', fakeAsync(() => {
    const errorResponse = new HttpErrorResponse({ status: 500 });
    productFeedbackServiceMock.findProductFeedbacks.and.returnValue(
      throwError(() => errorResponse)
    );
    component.token = 'testToken';
    component.fetchFeedbacks();
    tick();
    expect(component.errorMessage).toBe(ERROR_MESSAGES.FETCH_FAILURE);
    expect(component.isAuthenticated).toBeFalse();
  }));

  it('should toggle between tabs correctly', fakeAsync(() => {
    component.isAuthenticated = true;
    fixture.detectChanges();

    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));

    historyTab.triggerEventHandler('click', null);
    tick();
    fixture.detectChanges();
    expect(component.activeTab).toBe('history');
    expect(historyTab.classes['active']).toBeTrue();
    expect(reviewTab.classes['active']).toBeUndefined();

    reviewTab.triggerEventHandler('click', null);
    tick();
    fixture.detectChanges();
    expect(component.activeTab).toBe('review');
    expect(reviewTab.classes['active']).toBeTrue();
    expect(historyTab.classes['active']).toBeUndefined();
  }));

  it('should call handleError when fetchUserInfo fails', fakeAsync(() => {
    const errorResponse = new HttpErrorResponse({
      status: 500,
      statusText: 'Internal Server Error'
    });

    spyOn(component as any, 'handleError');

    authServiceMock.getUserInfo.and.returnValue(
      throwError(() => errorResponse)
    );

    component.token = 'test-token';
    component.fetchUserInfo();
    tick();

    expect((component as any).handleError).toHaveBeenCalledWith(errorResponse);
    expect(component.moderatorName).toBeUndefined();
  }));
});
