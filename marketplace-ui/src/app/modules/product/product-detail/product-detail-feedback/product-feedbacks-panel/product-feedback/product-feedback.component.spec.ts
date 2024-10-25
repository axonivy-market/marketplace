import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductFeedbackComponent } from './product-feedback.component';
import { CommonModule } from '@angular/common';
import { StarRatingComponent } from '../../../../../../shared/components/star-rating/star-rating.component';
import { ElementRef } from '@angular/core';
import { Feedback } from '../../../../../../shared/models/feedback.model';
import { MissingTranslationHandler, TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { httpLoaderFactory } from '../../../../../../core/configs/translate.config';

describe('ProductFeedbackComponent', () => {
  let component: ProductFeedbackComponent;
  let fixture: ComponentFixture<ProductFeedbackComponent>;
  let mockElementRef: ElementRef;

  beforeEach(async () => {
    mockElementRef = {
      nativeElement: {
        scrollHeight: 200,
        clientHeight: 100
      } as HTMLElement
    } as ElementRef;

    await TestBed.configureTestingModule({
      imports: [ProductFeedbackComponent, StarRatingComponent, CommonModule],
      providers: [
        { provide: ElementRef, useValue: mockElementRef },
        TranslateService
      ]
    }).compileComponents();


  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useFactory: httpLoaderFactory,
          },
          missingTranslationHandler: {
            provide: MissingTranslationHandler,
            useValue: { handle: () => 'Translation missing' }
          }
        })
      ]
    });

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
    expect(component.isExpanded()).toBe(true);

    component.toggleContent();
    expect(component.isExpanded()).toBe(false);
  });
});

