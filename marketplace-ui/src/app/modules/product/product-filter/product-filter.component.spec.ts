import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { By } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductFilterComponent } from './product-filter.component';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { of } from 'rxjs';
import { FILTER_TYPES, SORT_TYPES } from '../../../shared/constants/common.constant';

declare const viewport: Viewport;

describe('ProductFilterComponent', () => {
  let component: ProductFilterComponent;
  let fixture: ComponentFixture<ProductFilterComponent>;
  let activatedRoute: ActivatedRoute;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);
    await TestBed.configureTestingModule({
      imports: [
        ProductFilterComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [TranslateService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        { provide: Router, useValue: routerSpyObj }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ProductFilterComponent);
    component = fixture.componentInstance;
    activatedRoute = TestBed.inject(ActivatedRoute);
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
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
    expect(component.selectedTypeLabel).toEqual('common.filter.value.connector');
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
    fixture.detectChanges();
    expect(component.selectedSortLabel).toEqual('common.sort.value.standard');
  });


  it('search should update searchText correctly', () => {
    const searchText = 'portal';
    const input = fixture.debugElement.query(By.css('input')).nativeElement;
    input.value = searchText;
    input.dispatchEvent(new Event('input'));
    expect(component.searchText).toEqual(searchText);
  });

  it('should initialize with default type and sort if query params are empty', () => {
    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[0].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[0].label);
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: {},
      queryParamsHandling: ''
    });
  });

  it('should use valid type and sort from query params', () => {
    const validType = FILTER_TYPES[1].value;
    const validSort = SORT_TYPES[1].value;
    activatedRoute.queryParams = of({ type: validType, sort: validSort });

    fixture = TestBed.createComponent(ProductFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[1].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[1].label);
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: { type: validType, sort: validSort },
      queryParamsHandling: ''
    });
  });

  it('should revert to default type and sort if query params are invalid', () => {
    activatedRoute.queryParams = of({ type: 'invalidType', sort: 'invalidSort' });

    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[0].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[0].label);
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: {},
      queryParamsHandling: ''
    });
  });

  it('should remove invalid type and sort from the URI', () => {
    const validSort = SORT_TYPES[1].value;
    activatedRoute.queryParams = of({ type: 'invalidType', sort: validSort });

    fixture = TestBed.createComponent(ProductFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: { sort: validSort },
      queryParamsHandling: ''
    });
  });

  it('should update searchText from query params', () => {
    const searchValue = 'search query';
    activatedRoute.queryParams = of({ search: searchValue });

    fixture = TestBed.createComponent(ProductFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.searchText).toBe(searchValue);
  });

  it('should adapt classes correctly when switching light, dark and light mode with different selected types', () => {
    component.themeService.isDarkMode.set(false);
    component.selectedTypeLabel = FILTER_TYPES[1].label;
    fixture.detectChanges();

    component.themeService.isDarkMode.set(true);
    fixture.detectChanges();

    component.themeService.isDarkMode.set(false);
    component.selectedTypeLabel = FILTER_TYPES[2].label;
    fixture.detectChanges();

    const filterElements = fixture.debugElement.queryAll(By.css('.filter-type'));
    // Check type connectors (not selected)
    expect(filterElements[0].nativeElement.classList).toContain('border-dark');
    expect(filterElements[0].nativeElement.classList).toContain('text-dark');
    expect(filterElements[0].nativeElement.classList).not.toContain('text-light');

    // Check type util (selected)
    expect(filterElements[2].nativeElement.classList).toContain('text-light');
    expect(filterElements[2].nativeElement.classList).toContain('border-0');
    expect(filterElements[2].nativeElement.classList).not.toContain('text-dark');
  });

  it('should emit sortChange event with the selected sort option', () => {
    spyOn(component.sortChange, 'emit');
    const selectedSort = SORT_TYPES[1].value;

    component.onSortChange(selectedSort);

    expect(component.sortChange.emit).toHaveBeenCalledWith(selectedSort);
  });

  it('should update selectedSortLabel with correct label when sort changes', () => {
    const selectedSort = SORT_TYPES[1].value;
    component.onSortChange(selectedSort);

    expect(component.selectedSortLabel).toBe(SORT_TYPES[1].label);
  });
});
