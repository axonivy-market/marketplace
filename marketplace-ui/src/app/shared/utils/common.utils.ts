import { Language } from '../enums/language.enum';
import { ItemDropdown } from '../models/item-dropdown.model';
import { DisplayValue } from './../models/display-value.model';
import { CookieService } from 'ngx-cookie-service';

export class CommonUtils {
  static getLabel<T extends string>(value: string, options: ItemDropdown<T>[]): string {
    const currentLabel = options.find((option: ItemDropdown<T>) => option.value === value)?.label;
    return currentLabel ?? options[0].label;
  }

  static isContentDisplayedBasedOnLanguage(
    value: DisplayValue,
    language: Language
  ) {
    if (
      language === Language.DE &&
      value[language] !== '' &&
      value[language] !== undefined
    ) {
      return true;
    }

    return value[Language.EN] !== '' && value[Language.EN] !== undefined;
  }
  static getCookieValue<T>(
    cookieService: CookieService,
    cookieName: string,
    defaultValue: T
  ): T {
    const cookieValue = cookieService.get(cookieName);
    if (cookieValue === undefined || cookieValue === null || cookieValue === '') {
      return defaultValue;
    }

    switch (typeof defaultValue) {
      case 'boolean': {
        return (cookieValue.toLowerCase() === 'true') as unknown as T;
      }
      case 'number': {
        return parseFloat(cookieValue) as unknown as T;
      }
      default:
        return cookieValue as unknown as T;
    }
  }

}
