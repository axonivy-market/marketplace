// src/app/translate-universal-loader.ts
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { Injectable, Inject, PLATFORM_ID, TransferState, makeStateKey, inject } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

@Injectable()
export class TranslateUniversalLoader implements TranslateLoader {
  constructor(
    private readonly http: HttpClient,
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

    // Server-side: load from file system
    if (isPlatformServer(this.platformId)) {
      return this.loadTranslationsFromFileSystem(lang).pipe(
        tap(translations => {
          this.transferState.set(key, translations);
        })
      );
    }

    // Client-side: load via HTTP
    return this.http.get(`/assets/i18n/${lang}.json`).pipe(
      tap(translations => {
        this.transferState.set(key, translations);
      })
    );
  }

  private loadTranslationsFromFileSystem(lang: string): Observable<any> {
    try {
      // Use dynamic import to avoid issues with bundling
      const fs = require('fs');
      const path = require('path');
      
      // Try multiple possible paths based on different build outputs
    const possiblePaths = [
      path.join('/app', 'dist', 'browser', 'assets', 'i18n', `${lang}.json`), // Angular 15+ with browser folder
      path.join('/app', 'dist', 'assets', 'i18n', `${lang}.json`), // Older Angular versions
      path.join(process.cwd(), 'dist', 'browser', 'assets', 'i18n', `${lang}.json`),
      path.join(process.cwd(), 'dist', 'assets', 'i18n', `${lang}.json`)
    ];
        
        for (const translationPath of possiblePaths) {
            if (fs.existsSync(translationPath)) {
                console.log(`Found translation file at: ${translationPath}`);
                const translation = JSON.parse(fs.readFileSync(translationPath, 'utf8'));
                return of(translation);
            }
        }
        
        console.warn(`Translation file not found for ${lang}. Tried paths:`, possiblePaths);
        return of({});
    } catch (error) {
      console.error(`Error loading translation for ${lang}:`, error);
      return of({});
    }
  }
}

export function translateUniversalLoaderFactory(
  http: HttpClient, 
  transferState: TransferState,
  platformId: Object
) {
  return new TranslateUniversalLoader(http, transferState, platformId);
}