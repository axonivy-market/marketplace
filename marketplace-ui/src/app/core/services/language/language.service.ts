import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Language } from '../../../shared/enums/language.enum';

const DATA_LANGUAGE = 'data-language';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  constructor(@Inject(DOCUMENT) private readonly document: Document) {
    const localStorage = this.document.defaultView?.localStorage;
    if (localStorage) {
      this.loadDefaultLanguage(localStorage);
    }
  }

  loadDefaultLanguage(localStorage: Storage) {
    const language = localStorage.getItem(DATA_LANGUAGE);
    this.loadLanguage(language ?? Language.EN);
  }

  loadLanguage(language: string): void {
    localStorage.setItem(DATA_LANGUAGE, language);
  }

  getSelectedLanguage(): Language {
    return localStorage.getItem(DATA_LANGUAGE) as Language ?? Language.EN;
  }
}
