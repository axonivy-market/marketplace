import { ComponentFixture, TestBed } from '@angular/core/testing';

import { By } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import { SortOption } from '../../../shared/enums/sort-option.enum';
import { ProductFilterComponent } from './product-filter.component';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';

declare const viewport: Viewport;

describe('ProductFilterComponent', () => {
  let component: ProductFilterComponent;
  let fixture: ComponentFixture<ProductFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductFilterComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('onSelectedType should update selectedTypeOption correctly', () => {
    const filterElement = fixture.debugElement.queryAll(
      By.css('.filter-type')
    )[1].nativeElement as HTMLDivElement;

    filterElement.dispatchEvent(new Event('click'));
    expect(component.selectedType).toEqual(TypeOption.CONNECTORS);
  });

  it('filter type should change to selectbox in small screen', () => {
    viewport.set(540);
    const filterSelect = fixture.debugElement.query(
      By.css('.filter-type--select')
    );

    expect(getComputedStyle(filterSelect.nativeElement).display).not.toBe(
      'none'
    );
  });

  it('sort label should not display in small screen', () => {
    viewport.set(900);
    const sortLabel = fixture.debugElement.query(
      By.css('.sort-container__label')
    );
    expect(getComputedStyle(sortLabel.nativeElement).display).toBe('none');
  });

  it('onSortChange should update selectedSortOption correctly', () => {
    const select: HTMLSelectElement = fixture.debugElement.query(
      By.css('.sort-type')
    ).nativeElement;
    select.value = select.options[2].value;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.selectedSort).toEqual(SortOption.RECENT);
  });

  it('search should update searchText correctly', () => {
    const searchText = 'portal';
    const input = fixture.debugElement.query(By.css('input')).nativeElement;
    input.value = searchText;
    input.dispatchEvent(new Event('input'));
    expect(component.searchText).toEqual(searchText);
  });
});
