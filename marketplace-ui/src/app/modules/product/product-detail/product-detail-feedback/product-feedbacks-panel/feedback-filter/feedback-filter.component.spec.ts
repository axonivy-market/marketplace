import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FeedbackFilterComponent } from './feedback-filter.component';
import { ProductFeedbackService } from '../product-feedback.service';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';

describe('FeedbackFilterComponent', () => {
  let component: FeedbackFilterComponent;
  let fixture: ComponentFixture<FeedbackFilterComponent>;
  let translateService: jasmine.SpyObj<TranslateService>;
  let productFeedbackService: jasmine.SpyObj<ProductFeedbackService>;

  beforeEach(async () => {
    const productFeedbackServiceSpy = jasmine.createSpyObj('ProductFeedbackService', ['sort']);

    await TestBed.configureTestingModule({
      imports: [FeedbackFilterComponent, FormsModule, TranslateModule.forRoot() ],
      providers: [
        TranslateService,
        { provide: ProductFeedbackService, useValue: productFeedbackServiceSpy }
      ]
    })
    .compileComponents();

    translateService = TestBed.inject(TranslateService) as jasmine.SpyObj<TranslateService>;
    productFeedbackService = TestBed.inject(ProductFeedbackService) as jasmine.SpyObj<ProductFeedbackService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedbackFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render sort options from feedbackSortTypes', () => {
    const selectElement: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    const options = selectElement ? selectElement.querySelectorAll('option') : [];
  
    expect(options.length).toBe(FEEDBACK_SORT_TYPES.length);
  
    FEEDBACK_SORT_TYPES.forEach((type, index) => {
      const option = options[index] as HTMLOptionElement | null;
      expect(option).withContext('Option element should exist');
      if (option) {
        expect(option.textContent?.trim()).toBe(type.label);
        expect(option.value).toBe(type.sortFn);
      }
    });
  });

  it('should emit sortChange event on select change', () => {
    const selectElement: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    const emitSpy = spyOn(component.sortChange, 'emit').and.callThrough();

    selectElement.value = 'updatedAt,asc'; // Simulate select change
    selectElement.dispatchEvent(new Event('change'));

    expect(emitSpy).toHaveBeenCalledWith('updatedAt,asc');
  });
});