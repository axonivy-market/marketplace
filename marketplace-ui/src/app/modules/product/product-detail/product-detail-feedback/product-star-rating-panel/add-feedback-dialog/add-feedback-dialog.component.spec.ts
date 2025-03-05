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
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let productFeedbackServiceMock: jasmine.SpyObj<ProductFeedbackService>;
  let productDetailServiceMock: jasmine.SpyObj<ProductDetailService>;
  let activeModalMock: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getDisplayName'
    ]);
    const productFeedbackServiceSpy = jasmine.createSpyObj(
      'ProductFeedbackService',
      ['submitFeedback'],
      { userFeedback: signal({}), feedbacks: signal([]) }
    );
    const productDetailServiceSpy = jasmine.createSpyObj(
      'ProductDetailService',
      ['productId'],
      {
        productNames: signal({ en: 'en', de: 'de' }),
        productId: signal('mockProductId'),
        productLogoUrl: signal('logoUrl')
      }
    );
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', [
      'close',
      'dismiss'
    ]);

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
    authServiceMock = TestBed.inject(
      AuthService
    ) as jasmine.SpyObj<AuthService>;
    productFeedbackServiceMock = TestBed.inject(
      ProductFeedbackService
    ) as jasmine.SpyObj<ProductFeedbackService>;
    productDetailServiceMock = TestBed.inject(
      ProductDetailService
    ) as jasmine.SpyObj<ProductDetailService>;
    activeModalMock = TestBed.inject(
      NgbActiveModal
    ) as jasmine.SpyObj<NgbActiveModal>;
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
    authServiceMock.getDisplayName.and.returnValue(mockDisplayName);

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
      version: 0
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
      version: 0
    };

    component.feedback = mockFeedback;

    productFeedbackServiceMock.submitFeedback.and.returnValue(of(mockFeedback));

    component.onSubmitFeedback();

    expect(productFeedbackServiceMock.submitFeedback).toHaveBeenCalledWith(mockFeedback);
    expect(activeModalMock.close).toHaveBeenCalled();
  });
});
