import { Pipe, PipeTransform } from "@angular/core";
import { ProductModuleContent } from "../models/product-module-content.model";

@Pipe({
  standalone: true,
  name: 'missingReadmeContent'
})
export class MissingReadmeContentPipe implements PipeTransform {
  transform(productModuleContent: ProductModuleContent): boolean {
    return (
      !productModuleContent || Object.keys(productModuleContent).length === 0
    );
  }
}