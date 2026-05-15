import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SyncTaskDialogComponent } from './sync-task-dialog.component';
import { TranslateModule } from '@ngx-translate/core';
import { MarketProduct } from '../../../shared/models/product.model';
import { SYNC_TASK_KEYS } from '../../../shared/constants/admin.constant';

describe('SyncTaskDialogComponent', () => {
  let component: SyncTaskDialogComponent;
  let fixture: ComponentFixture<SyncTaskDialogComponent>;

  const mockProducts: MarketProduct[] = [
    { id: 'portal', marketDirectory: 'dir-portal' } as MarketProduct,
    { id: 'coffee', marketDirectory: 'dir-coffee' } as MarketProduct,
    { id: 'persistence', marketDirectory: 'dir-persistence' } as MarketProduct
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SyncTaskDialogComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(SyncTaskDialogComponent);
    component = fixture.componentInstance;

    component.products = mockProducts;
    component.taskKey = SYNC_TASK_KEYS.SYNC_ONE_PRODUCT as any;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should initialize filtered products when products change', () => {
      component.ngOnChanges({
        products: {
          currentValue: mockProducts,
          previousValue: [],
          firstChange: true,
          isFirstChange: () => true
        }
      });

      expect(component.filteredProducts().length).toBeGreaterThan(0);
    });

    it('should apply task config based on taskKey', () => {
      component.taskKey = SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS as any;

      component.ngOnChanges({
        taskKey: {
          currentValue: SYNC_TASK_KEYS.SYNC_ZIP_ARTIFACTS,
          previousValue: null,
          firstChange: true,
          isFirstChange: () => true
        }
      });

      expect(component.currentConfig.showAllOption).toBe(true);
    });

    it('should reset state when dialog becomes visible', () => {
      component.productSearch = 'portal';
      component.marketDirectory = 'something';

      component.ngOnChanges({
        visible: {
          currentValue: true,
          previousValue: false,
          firstChange: false,
          isFirstChange: () => false
        }
      });

      expect(component.productSearch).toBe('');
      expect(component.marketDirectory).toBe('');
      expect(component.dropdownOpen).toBe(false);
    });
  });

  describe('filterProducts', () => {
    it('should filter products by id', () => {
      component.productSearch = 'port';

      component.filterProducts();

      expect(component.filteredProducts()).toEqual([expect.objectContaining({ id: 'portal' })]);
    });

    it('should clear marketDirectory if no match and no all option', () => {
      component.currentConfig.showAllOption = false;
      component.productSearch = 'unknown';
      component.marketDirectory = 'dir';

      component.filterProducts();

      expect(component.marketDirectory).toBe('');
    });

    it('should not clear marketDirectory if showAllOption is true', () => {
      component.currentConfig.showAllOption = true;
      component.productSearch = 'unknown';
      component.marketDirectory = 'dir';

      component.filterProducts();

      expect(component.marketDirectory).toBe('dir');
    });
  });

  describe('selectProduct', () => {
    it('should set productSearch and marketDirectory', () => {
      const product = mockProducts[0];

      component.selectProduct(product);

      expect(component.productSearch).toBe(product.id);
      expect(component.marketDirectory).toBe(product.marketDirectory);
      expect(component.dropdownOpen).toBe(false);
    });
  });

  describe('isValid', () => {
    beforeEach(() => {
      component.currentConfig = {
        enableMarketPath: true,
        requireProduct: true,
        showAllOption: false,
        showOverrideCheckbox: false,
        dialogTitle: ''
      };
    });

    it('should return false when product is required but empty', () => {
      component.productSearch = '';

      expect(component.isValid()).toBe(false);
    });

    it('should return true when empty but showAllOption is enabled', () => {
      component.currentConfig.showAllOption = true;
      component.productSearch = '';

      expect(component.isValid()).toBe(true);
    });

    it('should return false when product does not exist', () => {
      component.productSearch = 'invalid';

      expect(component.isValid()).toBe(false);
    });

    it('should require marketDirectory when enabled', () => {
      component.productSearch = 'portal';
      component.marketDirectory = '';

      expect(component.isValid()).toBe(false);
    });

    it('should return true when valid product and marketDirectory provided', () => {
      component.productSearch = 'portal';
      component.marketDirectory = 'dir';

      expect(component.isValid()).toBe(true);
    });

    it('should always return true when requireProduct is false', () => {
      component.currentConfig.requireProduct = false;

      expect(component.isValid()).toBe(true);
    });
  });

  describe('onConfirm', () => {
    it('should emit confirmSync with normalized values', () => {
      const spy = vi.spyOn(component.confirmSync, 'emit');

      component.productSearch = ' portal ';
      component.marketDirectory = ' dir ';
      component.overrideMarketItemPath = true;

      component.onConfirm();

      expect(spy).toHaveBeenCalledWith({
        productId: 'portal',
        marketDirectory: 'dir',
        override: true
      });
    });
  });

  describe('onCancel', () => {
    it('should reset state and emit cancel event', () => {
      const spy = vi.spyOn(component.cancelSync, 'emit');

      component.productSearch = 'portal';
      component.marketDirectory = 'dir';
      component.dropdownOpen = true;

      component.onCancel();

      expect(component.productSearch).toBe('');
      expect(component.marketDirectory).toBe('');
      expect(component.dropdownOpen).toBe(false);
      expect(spy).toHaveBeenCalled();
    });
  });

  describe('dropdown behavior', () => {
    it('should open dropdown and load initial products', () => {
      component.openDropdown();

      expect(component.dropdownOpen).toBe(true);
      expect(component.filteredProducts().length).toBeGreaterThan(0);
    });
  });
});
