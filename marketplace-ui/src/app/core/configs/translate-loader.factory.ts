import { TranslateLoader } from '@ngx-translate/core';
import { of } from 'rxjs';
import { Injectable, Inject, PLATFORM_ID, TransferState, makeStateKey } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { APP, ASSETS, BROWSER, DIST, I18N, JSON_EXTENSION, SRC, UTF8 } from '../../shared/constants/common.constant';

const TRANSLATE_KEY = 'translations';
@Injectable()
export class TranslateUniversalLoader implements TranslateLoader {
  constructor(
    private readonly httpClient: HttpClient,
    private readonly transferState: TransferState,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) {}

  getTranslation(lang: string) {
    const key = makeStateKey<Object>(`${TRANSLATE_KEY}-${lang}`);

    // Check if we have cached translations in transfer state
    const cachedTranslations = this.transferState.get(key, null);
    if (cachedTranslations) {
      this.transferState.remove(key);
      return of(cachedTranslations);
    }

    if (isPlatformServer(this.platformId)) {
      return this.loadTranslationsFromFileSystem(lang).pipe(
        tap(translations => {
          this.transferState.set(key, translations);
        })
      );
    }

    return this.httpClient.get(`/${ASSETS}/${I18N}/${lang}${JSON_EXTENSION}`).pipe(
      tap(translations => {
        this.transferState.set(key, translations);
      })
    );
  }

  private loadTranslationsFromFileSystem(lang: string) {
    try {
      const fs = require('fs');
      const path = require('path');

      const languagePath = path.join(ASSETS, I18N, `${lang}${JSON_EXTENSION}`);
      // Try multiple possible paths based on different build outputs
      const i18nPaths = [
        path.join(`/${APP}`, DIST, BROWSER, languagePath), // For SSR by router
        path.join(languagePath), // For development environment
        path.join(process.cwd(), SRC, languagePath), // For CSR
        path.join(process.cwd(), DIST, BROWSER, languagePath) // For SSR by absolutely request
      ];

      for (const translationPath of i18nPaths) {
        if (fs.existsSync(translationPath)) {
          const translation = JSON.parse(
            fs.readFileSync(translationPath, UTF8)
          );
          return of(translation);
        }
      }
      return of({});
    } catch (error) {
      console.error(`Error loading translation for ${lang}: `, error);
      return of({});
    }
  }
}

export function translateUniversalLoaderFactory(httpClient: HttpClient, transferState: TransferState, platformId: Object) {
  return new TranslateUniversalLoader(httpClient, transferState, platformId);
}
