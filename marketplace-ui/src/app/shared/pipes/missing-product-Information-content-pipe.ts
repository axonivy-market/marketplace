import { Pipe, PipeTransform } from "@angular/core";
import { ProductDetail } from "../models/product-detail.model";

@Pipe({
  standalone: true,
  name: 'missingProductInformationContent'
})
export class MissingProductInformationContentPipe
  implements PipeTransform
{
  transform(productDetail: ProductDetail): boolean {
    return !productDetail || Object.keys(productDetail).length === 0;
  }
}