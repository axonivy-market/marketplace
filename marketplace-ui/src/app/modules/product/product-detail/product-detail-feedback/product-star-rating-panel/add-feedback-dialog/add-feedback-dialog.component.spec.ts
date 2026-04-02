import { beforeEach, describe, expect, it, vi, type MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../../../../../auth/auth.service';
import { ProductDetailService } from '../../../product-detail.service';
import { ProductFeedbackService } from '../../product-feedbacks-panel/product-feedback.service';
import { AddFeedbackDialogComponent } from './add-feedback-dialog.component';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { FeedbackStatus } from '../../../../../../shared/enums/feedback-status.enum';

describe('AddFeedbackDialogComponent', () => {
  let component: AddFeedbackDialogComponent;
  let fixture: ComponentFixture<AddFeedbackDialogComponent>;
  let authServiceMock: MockedObject<AuthService>;
  let productFeedbackServiceMock: MockedObject<ProductFeedbackService>;
  let activeModalMock: MockedObject<NgbActiveModal>;

  beforeEach(async () => {
    const authServiceSpy = {
      getDisplayName: vi.fn().mockName('AuthService.getDisplayName')
    };
    const productFeedbackServiceSpy = {
      submitFeedback: vi.fn().mockName('ProductFeedbackService.submitFeedback'),
      userFeedback: signal({}),
      feedbacks: signal([])
    };
    const productDetailServiceSpy = {
      productId: vi.fn().mockName('ProductDetailService.productId'),
      productNames: signal({ en: 'en', de: 'de' }),
      productLogoUrl: signal('logoUrl')
    };
    const activeModalSpy = {
      close: vi.fn().mockName('NgbActiveModal.close'),
      dismiss: vi.fn().mockName('NgbActiveModal.dismiss')
    };

    await TestBed.configureTestingModule({
      imports: [
        AddFeedbackDialogComponent,
        FormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ProductFeedbackService,
          useValue: productFeedbackServiceSpy
        },
        { provide: ProductDetailService, useValue: productDetailServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AddFeedbackDialogComponent);
    component = fixture.componentInstance;
    authServiceMock = TestBed.inject(AuthService) as MockedObject<AuthService>;
    productFeedbackServiceMock = TestBed.inject(
      ProductFeedbackService
    ) as MockedObject<ProductFeedbackService>;
    activeModalMock = TestBed.inject(
      NgbActiveModal
    ) as MockedObject<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddFeedbackDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize displayName with AuthService data on ngOnInit', () => {
    const mockDisplayName = 'John Doe';
    authServiceMock.getDisplayName.mockReturnValue(mockDisplayName);

    component.ngOnInit();

    expect(component.displayName).toBe(mockDisplayName);
  });

  it('should initialize feedback object on ngOnInit', () => {
    const mockDate = new Date('2025-02-25T17:51:56+07:00');
    const mockFeedback: Feedback = {
      content: 'Test feedback content',
      rating: 4,
      productId: 'mockProductId',
      feedbackStatus: FeedbackStatus.APPROVED,
      moderatorName: 'admin',
      reviewDate: mockDate,
      version: 0,
      productNames: { de: 'A-Trust', en: 'A-Trust' }
    };

    productFeedbackServiceMock.userFeedback.set(mockFeedback);

    component.ngOnInit();

    expect(component.feedback).toEqual(mockFeedback);
  });

  it('should submit feedback and close modal on onSubmitFeedback', () => {
    const mockFeedback: Feedback = {
      content: 'Test feedback content',
      rating: 4,
      productId: 'mockProductId',
      feedbackStatus: FeedbackStatus.APPROVED,
      moderatorName: 'admin',
      reviewDate: new Date(),
      version: 0,
      productNames: {}
    };

    component.feedback = mockFeedback;

    productFeedbackServiceMock.submitFeedback.mockReturnValue(of(mockFeedback));

    component.onSubmitFeedback();

    expect(productFeedbackServiceMock.submitFeedback).toHaveBeenCalledWith(
      mockFeedback
    );
    expect(activeModalMock.close).toHaveBeenCalled();
  });
});
