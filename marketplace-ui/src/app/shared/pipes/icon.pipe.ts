import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'productTypeIcon'
})
export class ProductTypeIconPipe implements PipeTransform {
  transform(value: string, _args?: []): string {
    switch (value) {
      case 'connector':
        return 'bi bi-plug';
      case 'solution':
        return 'bi bi-clipboard-check';
      case 'util':
        return 'bi bi-tools';
      default:
        return 'bi bi-grid';
    }
  }
}
