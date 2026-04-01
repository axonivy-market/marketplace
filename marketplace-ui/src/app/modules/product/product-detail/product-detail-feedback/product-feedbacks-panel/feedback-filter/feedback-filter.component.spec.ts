import type { MockedObject } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FeedbackFilterComponent } from './feedback-filter.component';
import { ProductFeedbackService } from '../product-feedback.service';
import { FEEDBACK_SORT_TYPES } from '../../../../../../shared/constants/common.constant';
import { By } from '@angular/platform-browser';
import { CommonDropdownComponent } from '../../../../../../shared/components/common-dropdown/common-dropdown.component';
import { ItemDropdown } from '../../../../../../shared/models/item-dropdown.model';
import { FeedbackSortType } from '../../../../../../shared/enums/feedback-sort-type';

describe('FeedbackFilterComponent', () => {
  let component: FeedbackFilterComponent;
  let fixture: ComponentFixture<FeedbackFilterComponent>;
  let translateService: MockedObject<TranslateService>;
  let productFeedbackService: MockedObject<ProductFeedbackService>;

  beforeEach(async () => {
    const sortSignal = vi.fn().mockReturnValue(undefined);
    (sortSignal as any).set = vi.fn();
    const productFeedbackServiceSpy = {
      sort: sortSignal
    };

    await TestBed.configureTestingModule({
      imports: [
        FeedbackFilterComponent,
        FormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        TranslateService,
        { provide: ProductFeedbackService, useValue: productFeedbackServiceSpy }
      ]
    }).compileComponents();

    translateService = TestBed.inject(
      TranslateService
    ) as MockedObject<TranslateService>;
    productFeedbackService = TestBed.inject(
      ProductFeedbackService
    ) as MockedObject<ProductFeedbackService>;
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
    const dropdownComponent = fixture.debugElement.query(
      By.directive(CommonDropdownComponent)
    ).componentInstance;
    expect(dropdownComponent.items.length).toBe(FEEDBACK_SORT_TYPES.length);
  });

  it('should pass the correct selected item to the dropdown', () => {
    const dropdownComponent = fixture.debugElement.query(
      By.directive(CommonDropdownComponent)
    ).componentInstance;
    expect(dropdownComponent.selectedItem).toBe(
      component.selectedSortTypeLabel()
    );
  });

  it('should call onSortChange when an item is selected', () => {
    vi.spyOn(component, 'onSortChange');
    const dropdownComponent = fixture.debugElement.query(
      By.directive(CommonDropdownComponent)
    ).componentInstance;
    const filterOption: ItemDropdown<FeedbackSortType> = {
      value: FeedbackSortType.NEWEST,
      label: 'Connectors' // Or whatever label is appropriate
    };

    dropdownComponent.itemSelected.emit(filterOption);
    expect(component.onSortChange).toHaveBeenCalledWith(filterOption);
  });

  it('should pass the correct items to the dropdown', () => {
    const dropdownComponent = fixture.debugElement.query(
      By.directive(CommonDropdownComponent)
    ).componentInstance;
    expect(dropdownComponent.items).toBe(component.feedbackSortTypes);
  });

  it('should update sort signal and emit sortChange when onSortChange is called', () => {
    const testValue: FeedbackSortType = FeedbackSortType.HIGHEST;
    const mockEvent: ItemDropdown<FeedbackSortType> = {
      value: testValue,
      label: 'common.sort.value.highest'
    };

    const emitSpy = vi.spyOn(component.sortChange, 'emit');

    component.productFeedbackService.sort = {
      set: vi.fn()
    } as any;

    component.onSortChange(mockEvent);

    expect(component.productFeedbackService.sort.set).toHaveBeenCalledWith(
      testValue
    );
    expect(emitSpy).toHaveBeenCalledWith(testValue);
  });
});
