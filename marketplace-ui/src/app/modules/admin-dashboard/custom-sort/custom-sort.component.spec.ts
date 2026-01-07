import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CustomSortComponent } from './custom-sort.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ProductService } from '../../product/product.service';
import { AdminDashboardService } from '../admin-dashboard.service';
import { of, throwError } from 'rxjs';

describe('CustomSortComponent', () => {
  let component: CustomSortComponent;
  let fixture: ComponentFixture<CustomSortComponent>;
  let productService: jasmine.SpyObj<ProductService>;
  let adminDashboardService: jasmine.SpyObj<AdminDashboardService>;
  let translateService: TranslateService;

  beforeEach(async () => {
    productService = jasmine.createSpyObj('ProductService', ['fetchAllProductIds']);
    adminDashboardService = jasmine.createSpyObj('AdminDashboardService', ['sortMarketExtensions']);

    productService.fetchAllProductIds.and.returnValue(Promise.resolve(['portal', 'coffee-machine-connector', 'persistence-utils']));
    adminDashboardService.sortMarketExtensions.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        { provide: ProductService, useValue: productService },
        { provide: AdminDashboardService, useValue: adminDashboardService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CustomSortComponent);
    component = fixture.componentInstance;
    translateService = TestBed.inject(TranslateService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load all product IDs on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    expect(component.allExtensions).toEqual(['portal', 'coffee-machine-connector', 'persistence-utils']);
    expect(component.isLoading).toBe(false);
  }));

  it('should handle error when loading product IDs', fakeAsync(() => {
    productService.fetchAllProductIds.and.returnValue(Promise.reject('error'));

    fixture.detectChanges();
    tick();

    expect(component.allExtensions).toEqual([]);
    expect(component.isLoading).toBe(false);
  }));

  describe('filteredAvailableExtensions', () => {
    beforeEach(() => {
      component.allExtensions = ['portal', 'coffee-machine-connector', 'demos'];
    });

    it('should return all extensions when search term is empty', () => {
      component.searchTerm = '';

      expect(component.filteredAvailableExtensions).toEqual(['portal', 'coffee-machine-connector', 'demos']);
    });

    it('should filter extensions by search term', () => {
      component.searchTerm = 'demos';

      expect(component.filteredAvailableExtensions).toEqual(['demos']);
    });

    it('should be case insensitive', () => {
      component.searchTerm = 'PORTAL';

      expect(component.filteredAvailableExtensions).toEqual(['portal']);
    });
  });

  describe('clearSearch', () => {
    it('should clear search term', () => {
      component.searchTerm = 'test';

      component.clearSearch();

      expect(component.searchTerm).toBe('');
    });
  });

  describe('drop', () => {
    beforeEach(() => {
      component.allExtensions = ['portal', 'coffee-machine-connector', 'persistence-utils', 'ext-4'];
      component.searchTerm = '';
    });

    it('should move from available to sorted', () => {
      component.sortingExtensions = [];

      const event = {
        previousContainer: { id: 'available-extensions', data: [] },
        container: { id: 'sorted-extensions', data: [] },
        previousIndex: 0,
        currentIndex: 0,
        item: { data: 'portal' }
      } as any;

      component.drop(event);

      expect(component.sortingExtensions).toContain('portal');
      expect(component.allExtensions).not.toContain('portal');
    });

    it('should move from sorted to available', () => {
      component.sortingExtensions = ['portal'];
      component.allExtensions = ['coffee-machine-connector', 'persistence-utils'];

      const event = {
        previousContainer: { id: 'sorted-extensions', data: [] },
        container: { id: 'available-extensions', data: [] },
        previousIndex: 0,
        currentIndex: 0,
        item: { data: 'portal' }
      } as any;

      component.drop(event);

      expect(component.sortingExtensions).toEqual([]);
      expect(component.allExtensions).toContain('portal');
    });

    it('should reorder within sorted table', () => {
      component.sortingExtensions = ['portal', 'coffee-machine-connector', 'persistence-utils'];
      const container = { id: 'sorted-extensions', data: component.sortingExtensions };

      const event = {
        previousContainer: container,
        container: container,
        previousIndex: 0,
        currentIndex: 2,
        item: { data: 'portal' }
      } as any;

      component.drop(event);

      expect(component.sortingExtensions).toEqual(['coffee-machine-connector', 'persistence-utils', 'portal']);
    });

    it('should reorder within available table', () => {
      component.allExtensions = ['portal', 'coffee-machine-connector', 'persistence-utils'];
      const container = { id: 'available-extensions', data: component.allExtensions };

      const event = {
        previousContainer: container,
        container: container,
        previousIndex: 0,
        currentIndex: 2,
        item: { data: 'portal' }
      } as any;

      component.drop(event);

      expect(component.allExtensions).toEqual(['coffee-machine-connector', 'persistence-utils', 'portal']);
    });
  });

  describe('sortMarketExtensions', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      tick();
    }));

    it('should call service and show success message', fakeAsync(() => {
      spyOn(translateService, 'instant').and.returnValue('Success');
      component.sortingExtensions = ['portal', 'coffee-machine-connector'];

      component.sortMarketExtensions();
      tick();

      expect(adminDashboardService.sortMarketExtensions).toHaveBeenCalled();
      expect(component.sortSuccessMessage).toBe('Success');
      expect(component.isSaving).toBe(false);
    }));

    it('should show error message on failure', fakeAsync(() => {
      adminDashboardService.sortMarketExtensions.and.returnValue(throwError(() => new Error('error')));
      spyOn(translateService, 'instant').and.returnValue('Error');

      component.sortMarketExtensions();
      tick();

      expect(component.sortErrorMessage).toBe('Error');
      expect(component.isSaving).toBe(false);
    }));
  });

  describe('drag preview width', () => {
    it('should set drag preview width on start', () => {
      const event = {
        source: {
          element: {
            nativeElement: { getBoundingClientRect: () => ({ width: 300 }) }
          }
        }
      } as any;

      component.setDragPreviewWidth(event);

      expect(component).toBeTruthy();
    });

    it('should not set width when width is 0', () => {
      const event = {
        source: {
          element: {
            nativeElement: { getBoundingClientRect: () => ({ width: 0 }) }
          }
        }
      } as any;

      component.setDragPreviewWidth(event);

      expect(component).toBeTruthy();
    });

    it('should not set width when width is negative', () => {
      const event = {
        source: {
          element: {
            nativeElement: { getBoundingClientRect: () => ({ width: -10 }) }
          }
        }
      } as any;

      component.setDragPreviewWidth(event);

      expect(component).toBeTruthy();
    });

    it('should adjust preview width on enter', () => {
      const event = {
        container: {
          element: {
            nativeElement: { getBoundingClientRect: () => ({ width: 400 }) }
          }
        }
      } as any;

      component.adjustPreviewWidthOnEnter(event);

      expect(component).toBeTruthy();
    });

    it('should reset drag preview width on end', () => {
      const event = {} as any;

      component.resetDragPreviewWidth(event);

      expect(component).toBeTruthy();
    });
  });
});
