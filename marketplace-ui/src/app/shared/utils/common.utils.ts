import { ItemDropdown } from '../models/item-dropdown.model';

export class CommonUtils {

  static getLabel<T extends string>(value: string, options: ItemDropdown<T>[]): string {
    const currentLabel = options.find((option: ItemDropdown<T>) => option.value === value)?.label;
    return currentLabel ?? options[0].label;
  }

}
