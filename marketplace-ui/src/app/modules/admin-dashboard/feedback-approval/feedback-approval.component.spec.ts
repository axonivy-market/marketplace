import { beforeEach, describe, expect, it, vi, type Mock, type MockedObject } from 'vitest';
import {
  ComponentFixture,
  TestBed
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
  let authServiceMock: MockedObject<AuthService>;
  let productFeedbackServiceMock: MockedObject<ProductFeedbackService>;

  beforeEach(async () => {
    const authSpy = {
      getToken: vi.fn().mockName('AuthService.getToken'),
      redirectToGitHub: vi.fn().mockName('AuthService.redirectToGitHub'),
      getDisplayName: vi.fn().mockName('AuthService.getDisplayName'),
      getUserInfo: vi.fn().mockName('AuthService.getUserInfo'),
      getDisplayNameFromAccessToken: vi
        .fn()
        .mockName('AuthService.getDisplayNameFromAccessToken'),
      decodeToken: vi.fn().mockName('AuthService.decodeToken')
    };
    authSpy.getDisplayName.mockReturnValue('TestUser');
    authSpy.getDisplayNameFromAccessToken.mockReturnValue(of('TestUser'));
    authSpy.getUserInfo.mockReturnValue(of({ name: 'TestUser' }));
    authSpy.decodeToken.mockReturnValue({ accessToken: 'decodedAccessToken' });

    const productFeedbackSpy = {
      findProductFeedbacks: vi
        .fn()
        .mockName('ProductFeedbackService.findProductFeedbacks'),
      updateFeedbackStatus: vi
        .fn()
        .mockName('ProductFeedbackService.updateFeedbackStatus')
    } as any;
    productFeedbackSpy.allFeedbacks = signal([]);
    productFeedbackSpy.pendingFeedbacks = signal([]);
    productFeedbackSpy.findProductFeedbacks.mockReturnValue(of([]));
    productFeedbackSpy.updateFeedbackStatus.mockReturnValue(of(null));

    await TestBed.configureTestingModule({
      imports: [FeedbackApprovalComponent, TranslateModule.forRoot()],
      providers: [
        ThemeService,
        { provide: AuthService, useValue: authSpy },
        { provide: AppModalService, useValue: {} },
        { provide: ProductFeedbackService, useValue: productFeedbackSpy },
        {
          provide: LanguageService,
          useValue: {
            selectedLanguage: vi
              .fn()
              .mockName('LanguageService.selectedLanguage')
          }
        },
        { provide: ActivatedRoute, useValue: {} }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedbackApprovalComponent);
    component = fixture.componentInstance;
    authServiceMock = TestBed.inject(AuthService) as MockedObject<AuthService>;
    productFeedbackServiceMock = TestBed.inject(
      ProductFeedbackService
    ) as MockedObject<ProductFeedbackService>;

    // Mock sessionStorage with valid JSON UserInfo
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    vi.spyOn(sessionStorage, 'getItem').mockReturnValue(null);
    vi.spyOn(sessionStorage, 'setItem');
    vi.spyOn(sessionStorage, 'removeItem');

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should update feedback status and refresh', () => {
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

    productFeedbackServiceMock.updateFeedbackStatus.mockReturnValue(
      of(updatedFeedback)
    );

    component.moderatorName = 'TestUser';
    vi.spyOn(component, 'fetchFeedbacks');
    component.onClickReviewButton(feedback, true);

    expect(
      productFeedbackServiceMock.updateFeedbackStatus
    ).toHaveBeenCalledWith(MOCK_APPROVED_FEEDBACK);
  });

  it('should update activeTab', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
  });

  it('should update activeTab when setActiveTab is called', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
  });

  it('should switch to history tab when clicked', () => {
    component.isAuthenticated = true;
    fixture.detectChanges();

    const historyTab = fixture.debugElement.query(By.css('#history-tab'));
    historyTab.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.activeTab).toBe('history');
    expect(historyTab.classes['active']).toBe(true);

    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    expect(reviewTab.classes['active']).toBeUndefined();

    const reviewPane = fixture.debugElement.query(
      By.css('.tab-pane[aria-labelledby="review-tab"]')
    );
    const historyPane = fixture.debugElement.query(
      By.css('.tab-pane[aria-labelledby="history-tab"]')
    );
    expect(reviewPane.classes['active']).toBeUndefined();
    expect(historyPane.classes['active']).toBe(true);
  });

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
    expect(reviewTable.componentInstance.isHistoryTab).toBe(false);
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
    expect(historyTable.componentInstance.isHistoryTab).toBe(true);
  });

  it('should initialize with stored token', () => {
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    (sessionStorage.getItem as Mock).mockReturnValue(JSON.stringify(mockUserInfo));
    
    // Need to create a new component instance to test ngOnInit
    const newFixture = TestBed.createComponent(FeedbackApprovalComponent);
    const newComponent = newFixture.componentInstance;
    const fetchFeedbacksSpy = vi.spyOn(newComponent, 'fetchFeedbacks');

    newFixture.detectChanges(); // This triggers ngOnInit

    expect(fetchFeedbacksSpy).toHaveBeenCalled();
  });

  it('should handle fetchUserInfo success', () => {
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    (sessionStorage.getItem as Mock).mockReturnValue(JSON.stringify(mockUserInfo));

    component.fetchUserInfo().subscribe(result => {
      expect(result).toBe('TestUser');
      expect(component.isAuthenticated).toBe(true);
      expect(component.moderatorName).toBe('TestUser');
    });
  });

  it('should handle non-unauthorized errors in fetchFeedbacks', () => {
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    (sessionStorage.getItem as Mock).mockReturnValue(JSON.stringify(mockUserInfo));

    const errorResponse = new HttpErrorResponse({ status: 500 });
    productFeedbackServiceMock.findProductFeedbacks.mockReturnValue(
      throwError(() => errorResponse)
    );

    vi.useFakeTimers();
    component.fetchFeedbacks();
    vi.runAllTimers();

    expect(component.errorMessage).toBe(ERROR_MESSAGES.FETCH_FAILURE);
    expect(component.isLoading).toBe(false);
    vi.useRealTimers();
  });

  it('should handle unauthorized error in fetchFeedbacks', () => {
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    (sessionStorage.getItem as Mock).mockReturnValue(JSON.stringify(mockUserInfo));

    const errorResponse = new HttpErrorResponse({ status: 401 });
    productFeedbackServiceMock.findProductFeedbacks.mockReturnValue(
      throwError(() => errorResponse)
    );

    vi.useFakeTimers();
    component.fetchFeedbacks();
    vi.runAllTimers();

    expect(component.errorMessage).toBe(ERROR_MESSAGES.INVALID_TOKEN);
    expect(component.isLoading).toBe(false);
    vi.useRealTimers();
  });

  it('should return early from fetchFeedbacks if not authenticated', () => {
    // Mock sessionStorage to return null so authentication fails
    (sessionStorage.getItem as Mock).mockReturnValue(null);
    component.isAuthenticated = false;

    vi.useFakeTimers();
    component.fetchFeedbacks();
    vi.runAllTimers();

    // When fetchUserInfo returns EMPTY (no user info), isLoading is set to false by finalize
    expect(component.isLoading).toBe(false);
    vi.useRealTimers();
  });

  it('should toggle between tabs correctly', () => {
    const mockUserInfo = {
      login: 'testuser',
      name: 'TestUser',
      avatarUrl: 'https://example.com/avatar.jpg',
      url: 'https://github.com/testuser',
      token: 'testToken'
    };
    (sessionStorage.getItem as Mock).mockReturnValue(JSON.stringify(mockUserInfo));

    component.isAuthenticated = true;
    fixture.detectChanges();

    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));

    if (historyTab) {
      historyTab.triggerEventHandler('click', null);
      fixture.detectChanges();
      expect(component.activeTab).toBe('history');
      expect(historyTab.classes['active']).toBe(true);
      expect(reviewTab.classes['active']).toBeUndefined();
    }

    if (reviewTab) {
      reviewTab.triggerEventHandler('click', null);
      fixture.detectChanges();
      expect(component.activeTab).toBe('review');
      expect(reviewTab.classes['active']).toBe(true);
      expect(historyTab.classes['active']).toBeUndefined();
    }
  });

  it('should call handleError when fetchUserInfo fails', () => {
    // Mock sessionStorage to return null - no user info, so it returns EMPTY
    (sessionStorage.getItem as Mock).mockReturnValue(null);

    component.fetchUserInfo().subscribe({
      complete: () => {
        // fetchUserInfo returns EMPTY when no user info is found
        expect(component.moderatorName).toBeUndefined();
      }
    });
  });
});
