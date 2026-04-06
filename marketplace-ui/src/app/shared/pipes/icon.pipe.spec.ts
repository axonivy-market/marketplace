import { TestBed } from '@angular/core/testing';
import { ProductTypeIconPipe } from './icon.pipe';

describe('ProductTypeIconPipe', () => {
  let pipe: ProductTypeIconPipe;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProductTypeIconPipe]
    });
    pipe = TestBed.inject(ProductTypeIconPipe);
  });

  it('should return ti ti-plug for connector', () => {
    expect(pipe.transform('connector')).toBe('ti ti-plug');
  });

  it('should return ti ti-clipboard-check for solution', () => {
    expect(pipe.transform('solution')).toBe('ti ti-clipboard-check');
  });

  it('should return ti ti-tools for util', () => {
    expect(pipe.transform('util')).toBe('ti ti-tools');
  });

  it('should return ti ti-grid for unknown value', () => {
    expect(pipe.transform('unknown')).toBe('ti ti-grid');
  });

  it('should return ti ti-grid for empty string', () => {
    expect(pipe.transform('')).toBe('ti ti-grid');
  });
});
