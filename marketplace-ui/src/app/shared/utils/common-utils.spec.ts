import { vi, describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { CookieService } from 'ngx-cookie-service';
import { ItemDropdown } from '../models/item-dropdown.model';
import { CommonUtils } from './common.utils';

describe('CommonUtils', () => {
  describe('getLabel', () => {
    it('should return the label for the matching value', () => {
      // Arrange
      const options: ItemDropdown<string>[] = [
        { value: 'value1', label: 'Label 1' },
        { value: 'value2', label: 'Label 2' }
      ];
      const value = 'value1';

      // Act
      const result = CommonUtils.getLabel(value, options);

      // Assert
      expect(result).toBe('Label 1');
    });

    it('should return the first label if no matching value is found', () => {
      // Arrange
      const options: ItemDropdown<string>[] = [
        { value: 'value1', label: 'Label 1' },
        { value: 'value2', label: 'Label 2' }
      ];
      const value = 'nonexistent';

      // Act
      const result = CommonUtils.getLabel(value, options);

      // Assert
      expect(result).toBe('Label 1');
    });
  });

  describe('getCookieValue', () => {
    let cookieService: CookieService;

    beforeEach(() => {
      cookieService = TestBed.inject(CookieService);
    });

    it('should return the default value if the cookie does not exist', () => {
      // Arrange
      vi.spyOn(cookieService, 'get').mockReturnValue('');
      const defaultValue = 'defaultString';

      // Act
      const result = CommonUtils.getCookieValue(
        cookieService,
        'nonexistentCookie',
        defaultValue
      );

      // Assert
      expect(result).toBe(defaultValue);
    });

    it('should return a boolean value when default value is boolean', () => {
      // Arrange
      vi.spyOn(cookieService, 'get').mockReturnValue('true');
      const defaultValue = false;

      // Act
      const result = CommonUtils.getCookieValue(
        cookieService,
        'booleanCookie',
        defaultValue
      );

      // Assert
      expect(result).toBe(true);
    });

    it('should return a number value when default value is number', () => {
      // Arrange
      vi.spyOn(cookieService, 'get').mockReturnValue('123.45');
      const defaultValue = 0;

      // Act
      const result = CommonUtils.getCookieValue(
        cookieService,
        'numberCookie',
        defaultValue
      );

      // Assert
      expect(result).toBe(123.45);
    });

    it('should return a string value when default value is string', () => {
      // Arrange
      vi.spyOn(cookieService, 'get').mockReturnValue('cookieValue');
      const defaultValue = 'defaultString';

      // Act
      const result = CommonUtils.getCookieValue(
        cookieService,
        'stringCookie',
        defaultValue
      );

      // Assert
      expect(result).toBe('cookieValue');
    });
  });
});
