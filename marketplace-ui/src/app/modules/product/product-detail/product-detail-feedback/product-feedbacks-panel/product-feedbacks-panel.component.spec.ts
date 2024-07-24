import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductFeedbacksPanelComponent } from './product-feedbacks-panel.component';
import { FeedbackFilterComponent } from './feedback-filter/feedback-filter.component';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeService } from '../../../../../core/services/theme/theme.service';
import { ProductFeedbackService } from './product-feedback.service';
import { ProductDetailService } from '../../product-detail.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('ProductFeedbacksPanelComponent', () => {
  let component: ProductFeedbacksPanelComponent;
  let fixture: ComponentFixture<ProductFeedbacksPanelComponent>;
  let productFeedbackService: ProductFeedbackService;
  let translateService: TranslateService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ ProductFeedbacksPanelComponent, FeedbackFilterComponent, CommonModule, TranslateModule.forRoot() ],
      providers: [
        ProductFeedbackService,
        ProductDetailService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        ThemeService,
        TranslateService
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductFeedbacksPanelComponent);
    component = fixture.componentInstance;
    productFeedbackService = TestBed.inject(ProductFeedbackService);

    component.isRenderInModalDialog = false;
    fixture.componentRef.setInput(
      'isMobileMode',
      false
    );
    spyOn(productFeedbackService, 'changeSort').and.callThrough();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should trigger sorting when sortChange event is emitted', () => {
    const sortType = 'createdAt,desc';
    component.onSortChange(sortType);
    expect(productFeedbackService.changeSort).toHaveBeenCalledWith(sortType);
  });

  it('should load more feedbacks on scroll check if not all loaded', () => {
    const mockEvent = {
      target: {
        scrollTop: 200,
        offsetHeight: 200,
        scrollHeight: 400
      }
    } as any;
  
    spyOn(productFeedbackService, 'areAllFeedbacksLoaded').and.returnValue(false);
    spyOn(productFeedbackService, 'loadMoreFeedbacks').and.callThrough();
  
    component.onScrollCheckAllFeedbacksLoaded(mockEvent);
  
    expect(productFeedbackService.loadMoreFeedbacks).toHaveBeenCalled();
  });
});
