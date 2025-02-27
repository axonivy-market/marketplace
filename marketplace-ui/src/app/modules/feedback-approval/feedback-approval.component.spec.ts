import { ComponentFixture, TestBed } from '@angular/core/testing';
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

describe('FeedbackApprovalComponent', () => {
  let component: FeedbackApprovalComponent;
  let fixture: ComponentFixture<FeedbackApprovalComponent>;
  let authServiceMock: any;
  let productFeedbackServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      getToken: jasmine.createSpy().and.returnValue('mockToken'),
      redirectToGitHub: jasmine.createSpy(),
      getDisplayName: jasmine.createSpy().and.returnValue('TestUser')
    };

    productFeedbackServiceMock = {
      allFeedbacks: of([]),
      pendingFeedbacks: of([]),
      findProductFeedbacks: jasmine.createSpy().and.returnValue(of([])),
      updateFeedbackStatus: jasmine.createSpy().and.returnValue(of(null))
    };

    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [FeedbackApprovalComponent, FeedbackTableComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: AppModalService, useValue: {} },
        { provide: ProductFeedbackService, useValue: productFeedbackServiceMock },
        { provide: LanguageService, useValue: {} },
        { provide: ThemeService, useValue: { isDarkMode: () => false } },
        { provide: ActivatedRoute, useValue: {} }
      ],
    }).compileComponents();

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


  it('should call redirectToGitHub if no token', () => {
    authServiceMock.getToken.and.returnValue(null);
    component.ngOnInit();
    expect(authServiceMock.redirectToGitHub).toHaveBeenCalledWith('feedback-approval');
  });


  it('should call findProductFeedbacks on fetchFeedbacks and set isAuthenticated', () => {
    component.fetchFeedbacks();
    expect(productFeedbackServiceMock.findProductFeedbacks).toHaveBeenCalled();
    expect(component.isAuthenticated).toBeTrue();
  });


  it('should set isAuthenticated to false on fetchFeedbacks error', () => {
    productFeedbackServiceMock.findProductFeedbacks.and.returnValue(throwError(() => new Error('Error')));
    component.fetchFeedbacks();
    expect(component.isAuthenticated).toBeFalse();
  });


  it('should call updateFeedbackStatus on onClickReviewButton', () => {
    const mockFeedback = { id: 1 } as any;
    component.onClickReviewButton(mockFeedback, true);
    expect(productFeedbackServiceMock.updateFeedbackStatus).toHaveBeenCalledWith(1, true, 'TestUser');
  });


  it('should update activeTab when setActiveTab is called', () => {
    component.setActiveTab('history');
    expect(component.activeTab).toBe('history');
  });


  it('should render review and history tabs', () => {
    fixture.detectChanges();
    const reviewTab = fixture.debugElement.query(By.css('#review-tab'));
    const historyTab = fixture.debugElement.query(By.css('#history-tab'));

    expect(reviewTab.nativeElement.textContent.trim()).toContain('common.approval.reviewFeedback');
    expect(historyTab.nativeElement.textContent.trim()).toContain('common.approval.history');
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
    productFeedbackServiceMock.pendingFeedbacks = of(mockPendingFeedbacks);
    productFeedbackServiceMock.allFeedbacks = of(mockFeedbacks);
    fixture.detectChanges();

    const reviewTable = fixture.debugElement.queryAll(By.directive(FeedbackTableComponent))[0];
    const historyTable = fixture.debugElement.queryAll(By.directive(FeedbackTableComponent))[1];

    expect(reviewTable.componentInstance.feedbacks).toEqual(mockPendingFeedbacks);
    expect(historyTable.componentInstance.feedbacks).toEqual(mockFeedbacks);
  });


  it('should trigger onClickReviewButton when review button is clicked', () => {
    spyOn(component, 'onClickReviewButton');

    const mockFeedback = { id: 1 } as any;
    productFeedbackServiceMock.pendingFeedbacks = of([mockFeedback]);
    fixture.detectChanges();

    const approveButton = fixture.debugElement.query(By.css('#approve-button'));
    approveButton.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.onClickReviewButton).toHaveBeenCalledWith(mockFeedback, true);
  });
});
