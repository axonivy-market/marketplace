import { Pipe, PipeTransform } from "@angular/core";
import { ProductModuleContent } from "../models/product-module-content.model";

@Pipe({
  standalone: true,
  name: 'hasValueTab'
})
export class HasValueTabPipe implements PipeTransform {
  transform(value: string, productModuleContent: ProductModuleContent): boolean {
    const conditions: { [key: string]: boolean } = {
      description: productModuleContent?.description !== null,
      demo: productModuleContent?.demo !== null,
      setup: productModuleContent?.setup !== null ,
      dependency: productModuleContent?.isDependency
    };

    return conditions[value] ?? false;
  }
}