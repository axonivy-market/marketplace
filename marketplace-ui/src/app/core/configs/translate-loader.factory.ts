import { TranslateLoader } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { Injectable, Inject, PLATFORM_ID, TransferState, makeStateKey } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

@Injectable()
export class TranslateUniversalLoader implements TranslateLoader {
  constructor(
    private readonly httpClient: HttpClient,
    private readonly transferState: TransferState,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) {}

  getTranslation(lang: string): Observable<any> {
    const key = makeStateKey<any>(`translations-${lang}`);

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

    return this.httpClient.get(`/assets/i18n/${lang}.json`).pipe(
      tap(translations => {
        this.transferState.set(key, translations);
      })
    );
  }

  private loadTranslationsFromFileSystem(lang: string): Observable<any> {
    try {
      const fs = require('fs');
      const path = require('path');

      // Try multiple possible paths based on different build outputs
      const i18nPaths = [
        path.join('/app', 'dist', 'browser', 'assets', 'i18n', `${lang}.json`), // For CSR
        path.join('assets', 'i18n', `${lang}.json`), // For development environment
        path.join(process.cwd(), 'src', 'assets', 'i18n', `${lang}.json`),
        path.join(process.cwd(), 'dist', 'browser', 'assets', 'i18n', `${lang}.json`
        )
      ];

      for (const translationPath of i18nPaths) {
        if (fs.existsSync(translationPath)) {
          const translation = JSON.parse(
            fs.readFileSync(translationPath, 'utf8')
          );
          return of(translation);
        }
      }
      return of({});
    } catch (error) {
      console.error(`Error loading translation for ${lang}:`, error);
      return of({});
    }
  }
}

export function translateUniversalLoaderFactory(httpClient: HttpClient, transferState: TransferState, platformId: Object) {
  return new TranslateUniversalLoader(httpClient, transferState, platformId);
}
