import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductFeedbackComponent } from './product-feedback.component';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { ElementRef } from '@angular/core';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { MissingTranslationHandler, TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { httpLoaderFactory } from '../../../../../../core/configs/translate.config';
import { ProductFeedbackService } from '../product-feedback.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../../../../../../auth/auth.service';
import { FeedbackStatus } from '../../../../../../shared/enums/feedback-status.enum';

describe('ProductFeedbackComponent', () => {
  let component: ProductFeedbackComponent;
  let fixture: ComponentFixture<ProductFeedbackComponent>;
  let mockElementRef: ElementRef;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [
      'getToken',
      'getUserId'
    ]);
    mockElementRef = {
      nativeElement: {
        scrollHeight: 200,
        clientHeight: 100
      } as HTMLElement
    } as ElementRef;

    await TestBed.configureTestingModule({
      imports: [
        ProductFeedbackComponent,
        StarRatingComponent,
        CommonModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useFactory: httpLoaderFactory
          },
          missingTranslationHandler: {
            provide: MissingTranslationHandler,
            useValue: { handle: () => 'Translation missing' }
          }
        })
      ],
      providers: [
        { provide: ElementRef, useValue: mockElementRef },
        { provide: AuthService, useValue: authServiceSpy },
        TranslateService,
        ProductFeedbackService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    authService = authServiceSpy;

    fixture = TestBed.createComponent(ProductFeedbackComponent);
    component = fixture.componentInstance;
    component.feedback = {
      username: 'Test User',
      userAvatarUrl: 'avatar-url',
      rating: 4,
      content: 'This is a test feedback content.'
    } as Feedback;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle content visibility when toggleContent is called', () => {
    component.toggleContent();
    expect(component.feedback.isExpanded).toBe(true);

    component.toggleContent();
    expect(component.feedback.isExpanded).toBe(false);
  });

  it('should update scrollHeight and clientHeight correctly via updateHeights()', () => {
    component['contentElement'] = mockElementRef;

    component['updateHeights']();

    expect(component['scrollHeight']()).toBe(200);
    expect(component['clientHeight']()).toBe(100);
  });

  it('isFeedbackPending should return false if userId is not the same as logged in user', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'User feedback',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.PENDING,
        moderatorName: '',
        userId: 'user1',
        version: 1,
        productNames: {}
      }
    ];
    component.feedback = mockFeedbacks[0];
    authService.getUserId.and.returnValue('user2');
    expect(component.isFeedbackPending(component.feedback)).toBe(false);
  });

  it('isFeedbackPending should return false if feedbackStatus is not PENDING', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'User feedback',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.APPROVED,
        moderatorName: '',
        userId: 'user1',
        version: 1,
        productNames: {}
      }
    ];
    component.feedback = mockFeedbacks[0];
    authService.getUserId.and.returnValue('user1');
    expect(component.isFeedbackPending(component.feedback)).toBe(false);
  });

  it('isFeedbackPending should return true if userId is the same with logged in user and feedbackStatus is PENDING', () => {
    const mockFeedbacks: Feedback[] = [
      {
        id: '1',
        content: 'User feedback',
        rating: 5,
        productId: '123',
        feedbackStatus: FeedbackStatus.PENDING,
        moderatorName: '',
        userId: 'user1',
        version: 1,
        productNames: {}
      }
    ];
    component.feedback = mockFeedbacks[0];
    authService.getUserId.and.returnValue('user1');
    expect(component.isFeedbackPending(component.feedback)).toBe(true);
  });
});
