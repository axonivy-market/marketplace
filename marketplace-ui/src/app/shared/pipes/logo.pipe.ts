import { Pipe, PipeTransform } from '@angular/core';
import { Product } from '../models/product.model';

@Pipe({
  standalone: true,
  name: 'logo'
})
export class ProductLogoPipe implements PipeTransform {
  transform(product: Product, _args?: []): string {
    let logo = product.logo;
    if (logo === undefined || logo === '') {
      return `/assets/images/misc/axonivy-logo-round.png`;
    }
    return "data:image/jpg;base64,".concat(logo);
  }
}
