import { describe, it, expect, beforeEach } from 'vitest';
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

  it('should return ti ti-clipboard-check for demo', () => {
    expect(pipe.transform('demo')).toBe('ti ti-clipboard-check');
  });

  it('should return ti ti-tools for utils', () => {
    expect(pipe.transform('utils')).toBe('ti ti-tools');
  });

  it('should return ti ti-grid for unknown value', () => {
    expect(pipe.transform('unknown')).toBe('ti ti-grid');
  });

  it('should return ti ti-grid for empty string', () => {
    expect(pipe.transform('')).toBe('ti ti-grid');
  });
});
