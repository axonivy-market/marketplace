import { CommonModule, DOCUMENT } from '@angular/common';
import { Component, inject } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LANGUAGES } from '../../../constants/common.constant';
import { Language } from '../../../enums/language.enum';
import { LanguageService } from '../../../../core/services/language/language.service';

@Component({
  selector: 'app-language-selection',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './language-selection.component.html',
  styleUrl: './language-selection.component.scss'
})
export class LanguageSelectionComponent {
  languages = LANGUAGES;
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  document = inject(DOCUMENT);
  currentLanguage: Language | null = null;

  constructor() {
    this.currentLanguage = this.document.defaultView?.localStorage.getItem(
      'data-language'
    ) as Language;
  }

  onSelectLanguage(language: Language) {
    this.translateService.setDefaultLang(language);
    this.translateService.use(language);
    this.languageService.loadLanguage(language);
    this.currentLanguage = language;
  }

  isActiveClass(language: Language): string {
    return this.currentLanguage === language ? 'active' : 'inactive';
  }
}
