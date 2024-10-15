import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'productType'
})
export class ProductTypePipe implements PipeTransform {
  transform(type: string, _args?: []): string {
    return `common.filter.value.${type}`;
  }
}
