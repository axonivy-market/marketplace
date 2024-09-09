import { inject, Pipe, PipeTransform } from '@angular/core';
import { LanguageService } from '../../core/services/language/language.service';
import { Language } from '../enums/language.enum';
import { DisplayValue } from '../models/display-value.model';
import { ProductModuleContent } from '../models/product-module-content.model';

@Pipe({
  standalone: true,
  name: 'hasValueTab'
})
export class HasValueTabPipe implements PipeTransform {
  // transform(value: string, productModuleContent: ProductModuleContent): boolean {
  //   const conditions: { [key: string]: boolean } = {
  //     description: productModuleContent?.description !== null,
  //     demo: productModuleContent?.demo !== null,
  //     setup: productModuleContent?.setup !== null ,
  //     dependency: productModuleContent?.isDependency
  //   };

  //   return conditions[value] ?? false;
  // }

  languageService = inject(LanguageService);

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
        this.isContentDisplayedBasedOnLanguage(
          productModuleContent.description,
          lang
        ),
      demo:
        productModuleContent.demo !== null &&
        this.isContentDisplayedBasedOnLanguage(productModuleContent.demo, lang),
      setup:
        productModuleContent.setup !== null &&
        this.isContentDisplayedBasedOnLanguage(
          productModuleContent.setup,
          lang
        ),
      dependency: productModuleContent.isDependency
    };

    return conditions[value] ?? false;
  }

  private isContentDisplayedBasedOnLanguage(
    value: DisplayValue,
    language: Language
  ) {
    if (
      language === Language.DE &&
      value[language] !== '' &&
      value[language] !== undefined
    ) {
      return true;
    }

    return value[Language.EN] !== '' && value[Language.EN] !== undefined;
  }
}
