import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FeedbackFilterComponent } from './feedback-filter.component';
import { ProductFeedbackService } from '../product-feedback.service';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { By } from '@angular/platform-browser';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';
import { ItemDropdown } from '../../../../../../shared/models/item-dropdown.model';
import { TypeOption } from '../../../../../../shared/enums/type-option.enum';
import { SortOption } from '../../../../../../shared/enums/sort-option.enum';
import { FeedbackSortType } from '../../../../../../shared/enums/feedback-sort-type';

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
    const dropdownComponent = fixture.debugElement.query(By.directive(CommonDropdownComponent)).componentInstance;
    expect(dropdownComponent.items.length).toBe(FEEDBACK_SORT_TYPES.length);
  });

  it('should pass the correct selected item to the dropdown', () => {
    const dropdownComponent = fixture.debugElement.query(By.directive(CommonDropdownComponent)).componentInstance;
    expect(dropdownComponent.selectedItem).toBe(component.selectedSortTypeLabel());
  });

  it('should call onSortChange when an item is selected', () => {
    spyOn(component, 'onSortChange');
    const dropdownComponent = fixture.debugElement.query(By.directive(CommonDropdownComponent)).componentInstance;
    const filterOption: ItemDropdown<FeedbackSortType> = {
      value: FeedbackSortType.NEWEST,
      label: 'Connectors' // Or whatever label is appropriate
    };

    dropdownComponent.itemSelected.emit(filterOption);
    expect(component.onSortChange).toHaveBeenCalledWith(filterOption);
  });

  it('should pass the correct items to the dropdown', () => {
    const dropdownComponent = fixture.debugElement.query(By.directive(CommonDropdownComponent)).componentInstance;
    expect(dropdownComponent.items).toBe(component.feedbackSortTypes);
  });
});
