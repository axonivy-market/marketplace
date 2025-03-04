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
import { ERROR_MESSAGES, FEEDBACK_APPROVAL_SESSION_TOKEN, UNAUTHORIZED } from '../../shared/constants/common.constant';

describe('FeedbackApprovalComponent', () => {
  let component: FeedbackApprovalComponent;
  let fixture: ComponentFixture<FeedbackApprovalComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let productFeedbackServiceMock: jasmine.SpyObj<ProductFeedbackService>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', [
      'getToken', 'redirectToGitHub', 'getDisplayName', 'getUserInfo', 'getDisplayNameFromAccessToken'
    ]);
    authSpy.getDisplayName.and.returnValue('TestUser');
    authSpy.getDisplayNameFromAccessToken.and.returnValue(of('TestUser'));
    authSpy.getUserInfo.and.returnValue(of({ name: 'TestUser' }));

    const productFeedbackSpy = jasmine.createSpyObj('ProductFeedbackService', [
      'findProductFeedbacks', 'updateFeedbackStatus'
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
        { provide: LanguageService, useValue: jasmine.createSpyObj('LanguageService', ['selectedLanguage']) },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedbackApprovalComponent);
    component = fixture.componentInstance;
    authServiceMock = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    productFeedbackServiceMock = TestBed.inject(ProductFeedbackService) as jasmine.SpyObj<ProductFeedbackService>;
    fixture.detectChanges();

    // Mock sessionStorage
    spyOn(sessionStorage, 'getItem').and.returnValue(null);
    spyOn(sessionStorage, 'setItem');
    spyOn(sessionStorage, 'removeItem');
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch feedbacks when token exists', () => {
    spyOn(component, 'fetchFeedbacks');
    component.token = 'mockToken';
    component.onSubmit();
    expect(component.errorMessage).toBe('');
    expect(component.fetchFeedbacks).toHaveBeenCalled();
  });

  it('should initialize without token', () => {
    spyOn(component, 'fetchFeedbacks');
    component.ngOnInit();
    expect(component.token).toBe('');
    expect(component.isAuthenticated).toBeFalse();
    expect(component.fetchFeedbacks).not.toHaveBeenCalled();
  });

  it('should set error message when no token', () => {
    component.token = '';
    component.onSubmit();
    expect(component.errorMessage).toBe(ERROR_MESSAGES.TOKEN_REQUIRED);
    expect(component.isAuthenticated).toBeFalse();
  });

  it('should set session token and update authentication state', () => {
    component.token = 'mockToken';
    component.fetchFeedbacks();
    expect(sessionStorage.setItem).toHaveBeenCalledWith(FEEDBACK_APPROVAL_SESSION_TOKEN, 'mockToken');
    expect(component.isAuthenticated).toBeTrue();
  });

  it('should handle error and clear session', fakeAsync(() => {
    productFeedbackServiceMock.findProductFeedbacks.and.returnValue(throwError(() => ({
      status: UNAUTHORIZED
    })));
    component.token = 'mockToken';
    component.fetchFeedbacks();
    tick();
    expect(component.errorMessage).toBe(ERROR_MESSAGES.UNAUTHORIZED_ACCESS);
    expect(component.isAuthenticated).toBeFalse();
    expect(sessionStorage.removeItem).toHaveBeenCalledWith(FEEDBACK_APPROVAL_SESSION_TOKEN);
  }));

  it('should update feedback status and refresh', () => {
    const feedback = { id: '1', version: 1 } as any;
    component.onClickReviewButton(feedback, true);
    expect(productFeedbackServiceMock.updateFeedbackStatus)
      .toHaveBeenCalledWith('1', true, 'TestUser', 1);
  });

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
});
