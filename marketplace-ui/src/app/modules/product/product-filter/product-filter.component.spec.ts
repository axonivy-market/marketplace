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
        MatomoTestingModule.forRoot(),
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
    activatedRoute.queryParams = of({});

    // const params = { someParam: 'someValue' };

    // // Mock the queryParams observable to emit params
    // (activatedRoute.queryParams as any) = of(params);

    component.ngOnInit();

    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[0].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[0].label);
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: { type: null, sort: null },
      queryParamsHandling: ''
    });
  });

  it('should use valid type and sort from query params', () => {
    const validType = FILTER_TYPES[1].value;
    const validSort = SORT_TYPES[1].value;
    activatedRoute.queryParams = of({ type: validType, sort: validSort });

    component.ngOnInit();

    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[1].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[1].label);
    expect(routerSpy.navigate).not.toHaveBeenCalled(); // No need to update valid params
  });

  it('should revert to default type and sort if query params are invalid', () => {
    activatedRoute.queryParams = of({ type: 'invalidType', sort: 'invalidSort' });
    component.ngOnInit();

    expect(component.selectedTypeLabel).toBe(FILTER_TYPES[0].label);
    expect(component.selectedSortLabel).toBe(SORT_TYPES[0].label);
    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: { type: null, sort: null },
      queryParamsHandling: ''
    });
  });

  it('should remove invalid type and sort from the URI', () => {
    activatedRoute.queryParams = of({ type: 'invalidType', sort: 'invalidSort' });

    component.ngOnInit();

    expect(routerSpy.navigate).toHaveBeenCalledWith([], {
      relativeTo: jasmine.anything(),
      queryParams: { type: null, sort: null },
      queryParamsHandling: ''
    });
  });

  it('should update searchText from query params', () => {
    const searchValue = 'search query';
    activatedRoute.queryParams = of({ search: searchValue });

    component.ngOnInit();

    expect(component.searchText).toBe(searchValue);
  });



  // it('should emit filterChange event on valid type', () => {
  //   spyOn(component.filterChange, 'emit');
  //   const validType = FILTER_TYPES[1].value;
  //   activatedRoute.queryParams = of({ type: validType });

  //   component.ngOnInit();

  //   expect(component.filterChange.emit).toHaveBeenCalledWith(jasmine.objectContaining({ value: validType }));
  // });

  // it('should emit sortChange event on valid sort', () => {
  //   spyOn(component.sortChange, 'emit');
  //   const validSort = SORT_TYPES[1].value;
  //   activatedRoute.queryParams = of({ sort: validSort });

  //   component.ngOnInit();

  //   expect(component.sortChange.emit).toHaveBeenCalledWith(validSort);
  // });

  // it('should emit default filterChange event on invalid type', () => {
  //   spyOn(component.filterChange, 'emit');
  //   activatedRoute.queryParams = of({ type: 'invalidType' });

  //   component.ngOnInit();

  //   expect(component.filterChange.emit).toHaveBeenCalledWith(jasmine.objectContaining({ value: FILTER_TYPES[0].value }));
  // });

  // it('should emit default sortChange event on invalid sort', () => {
  //   spyOn(component.sortChange, 'emit');
  //   activatedRoute.queryParams = of({ sort: 'invalidSort' });

  //   component.ngOnInit();

  //   expect(component.sortChange.emit).toHaveBeenCalledWith(SortOption.STANDARD);
  // });
});
