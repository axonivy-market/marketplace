import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'productType'
})
export class ProductTypePipe implements PipeTransform {
  transform(type: string, _args?: []): string {
    let i18nKey = '';
    if (type) {
      i18nKey = `common.filter.value.${type}`;
    }
    return i18nKey;
  }
}
