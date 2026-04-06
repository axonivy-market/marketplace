import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'productTypeIcon'
})
export class ProductTypeIconPipe implements PipeTransform {
  transform(value: string, _args?: []): string {
    switch (value) {
      case 'connector':
        return 'ti ti-plug';
      case 'solution':
        return 'ti ti-clipboard-check';
      case 'util':
        return 'ti ti-tools';
      default:
        return 'ti ti-grid';
    }
  }
}
