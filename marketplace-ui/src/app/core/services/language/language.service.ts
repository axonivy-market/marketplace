import { DOCUMENT } from '@angular/common';
import { computed, Inject, Injectable, signal } from '@angular/core';
import { Language } from '../../../shared/enums/language.enum';

const DATA_LANGUAGE = 'data-language';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly language = signal(Language.EN);
  selectedLanguage = computed(() => this.language() ?? Language.EN)

  constructor(@Inject(DOCUMENT) private readonly document: Document) {
    const localStorage = this.document.defaultView?.localStorage;
    if (localStorage) {
      this.loadDefaultLanguage(localStorage);
    }
  }

  loadDefaultLanguage(localStorage: Storage) {
    const language = localStorage.getItem(DATA_LANGUAGE) as Language;
    if (this.isValidLanguage(language)) {
      this.loadLanguage(language);
    } else {
      this.loadLanguage(Language.EN);
    }
  }

  private isValidLanguage(language: Language) {
    return Object.values(Language).includes(language);
  }

  loadLanguage(language: Language): void {
    localStorage.setItem(DATA_LANGUAGE, language);
    this.language.set(language);
  }
}
