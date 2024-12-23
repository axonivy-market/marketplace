import { Pipe, PipeTransform } from "@angular/core";
import { ProductDetail } from "../models/product-detail.model";

@Pipe({
  standalone: true,
  name: 'emptyProductDetailPipe'
})
export class EmptyProductDetailPipe
  implements PipeTransform
{
  transform(productDetail: ProductDetail): boolean {
    return !productDetail || Object.keys(productDetail).length === 0;
  }
}