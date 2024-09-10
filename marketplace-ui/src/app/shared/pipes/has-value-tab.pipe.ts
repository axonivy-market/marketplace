import { inject, Pipe, PipeTransform } from '@angular/core';
import { LanguageService } from '../../core/services/language/language.service';
import { Language } from '../enums/language.enum';
import { DisplayValue } from '../models/display-value.model';
import { ProductModuleContent } from '../models/product-module-content.model';
import { ProductDetailService } from '../../modules/product/product-detail/product-detail.service';
import { CommonUtils } from '../utils/common.utils';

@Pipe({
  standalone: true,
  name: 'hasValueTab'
})
export class HasValueTabPipe implements PipeTransform {
  languageService = inject(LanguageService);
  productDetailService = inject(ProductDetailService);

  transform(
    value: string,
    productModuleContent: ProductModuleContent,
    lang: Language
  ): boolean {
    if (Object.keys(productModuleContent).length === 0) {
      return false;
    }

    const conditions: { [key: string]: boolean } = {
      description:
        productModuleContent.description !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(
          productModuleContent.description,
          lang
        ),
      demo:
        productModuleContent.demo !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(productModuleContent.demo, lang),
      setup:
        productModuleContent.setup !== null &&
        CommonUtils.isContentDisplayedBasedOnLanguage(
          productModuleContent.setup,
          lang
        ),
      dependency: productModuleContent.isDependency
    };

    return conditions[value] ?? false;
  }
}
