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
import { FeedbackFilterService } from './feedback-filter.service';
import { Subject } from 'rxjs';

describe('FeedbackFilterComponent', () => {
  const mockEvent = {
    value: FeedbackSortType.NEWEST,
    label: 'common.sort.value.newest',
    sortFn: 'updatedAt,desc'
  } as ItemDropdown<FeedbackSortType>;

  let component: FeedbackFilterComponent;
  let fixture: ComponentFixture<FeedbackFilterComponent>;
  let translateService: jasmine.SpyObj<TranslateService>;
  let productFeedbackService: jasmine.SpyObj<ProductFeedbackService>;
  let feedbackFilterService: FeedbackFilterService;

  beforeEach(async () => {
    const productFeedbackServiceSpy = jasmine.createSpyObj('ProductFeedbackService', ['sort']);

    await TestBed.configureTestingModule({
      imports: [FeedbackFilterComponent, FormsModule, TranslateModule.forRoot() ],
      providers: [
        {
          provide: FeedbackFilterService,
          useValue: {
            event$: new Subject(),
            data: null,
            changeSortByLabel: jasmine.createSpy('changeSortByLabel')
          }
        },
        TranslateService,
        { provide: ProductFeedbackService, useValue: productFeedbackServiceSpy }
      ]
    })
    .compileComponents();

    translateService = TestBed.inject(TranslateService) as jasmine.SpyObj<TranslateService>;
    productFeedbackService = TestBed.inject(ProductFeedbackService) as jasmine.SpyObj<ProductFeedbackService>;
    feedbackFilterService = TestBed.inject(FeedbackFilterService);
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
    expect(dropdownComponent.selectedItem).toBe(component.selectedSortTypeLabel);
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

  it('should emit sortChange event when onSortChange is called', () => {
    spyOn(component.sortChange, 'emit');
    component.onSortChange(mockEvent);
    expect(component.sortChange.emit).toHaveBeenCalledWith(mockEvent.sortFn);
  });

  it('should listen to feedbackFilterService event$ and call changeSortByLabel', () => {
    spyOn(component, 'changeSortByLabel').and.callThrough();
    component.ngOnInit(); // Subscribes to event$
    (feedbackFilterService.event$ as Subject<any>).next(mockEvent); // Trigger the event
    expect(component.changeSortByLabel).toHaveBeenCalledWith(mockEvent);
  });

  it('should NOT call changeSortByLabel if feedbackFilterService.data does not exist', () => {
    feedbackFilterService.data = undefined;
    spyOn(component, 'changeSortByLabel');
    component.ngOnInit()
    expect(component.changeSortByLabel).not.toHaveBeenCalled();
  });

  it('should call changeSortByLabel if feedbackFilterService.data exists', () => {
    feedbackFilterService.data = mockEvent;
    spyOn(component, 'changeSortByLabel');
    component.ngOnInit()
    expect(component.changeSortByLabel).toHaveBeenCalledWith(mockEvent);
  });
});
