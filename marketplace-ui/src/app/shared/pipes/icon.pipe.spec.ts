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

  it('should return bi bi-plug for connector', () => {
    expect(pipe.transform('connector')).toBe('bi bi-plug');
  });

  it('should return bi bi-clipboard-check for solution', () => {
    expect(pipe.transform('solution')).toBe('bi bi-clipboard-check');
  });

  it('should return bi bi-tools for util', () => {
    expect(pipe.transform('util')).toBe('bi bi-tools');
  });

  it('should return bi bi-grid for unknown value', () => {
    expect(pipe.transform('unknown')).toBe('bi bi-grid');
  });

  it('should return bi bi-grid for empty string', () => {
    expect(pipe.transform('')).toBe('bi bi-grid');
  });
});
