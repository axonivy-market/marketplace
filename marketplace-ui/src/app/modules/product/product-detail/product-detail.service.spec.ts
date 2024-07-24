import { TestBed } from '@angular/core/testing';
import { ProductDetailService } from './product-detail.service';
import { DisplayValue } from '../../../shared/models/display-value.model';

describe('ProductDetailService', () => {
  let service: ProductDetailService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductDetailService]
    });
    service = TestBed.inject(ProductDetailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have a default productId signal', () => {
    expect(service.productId()).toBe('');
  });

  it('should update productId signal', () => {
    const newProductId = '12345';
    service.productId.set(newProductId);
    expect(service.productId()).toBe(newProductId);
  });

  it('should have a default productNames signal', () => {
    expect(service.productNames()).toEqual({} as DisplayValue);
  });

  it('should update productNames signal', () => {
    const newProductNames: DisplayValue = { en: 'en', de: 'de' };
    service.productNames.set(newProductNames);
    expect(service.productNames()).toEqual(newProductNames);
  });
});