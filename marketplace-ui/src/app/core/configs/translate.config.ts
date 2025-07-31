import { HttpClient } from '@angular/common/http';
import { TranslateLoader } from '@ngx-translate/core';
// import { map, Observable, of } from 'rxjs';
// import { parse } from 'yaml';
import { isPlatformServer } from '@angular/common';
import { Observable, of } from 'rxjs';
// import { readFileSync } from 'fs';
// import { join } from 'path';
// import { Injectable, Inject, PLATFORM_ID } from '@angular/core';

// // Client-side loader
// class TranslateYamlHttpLoader implements TranslateLoader {
//   constructor(
//     private readonly http: HttpClient,
//     public path = '/assets/i18n/'
//   ) {}

//   public getTranslation(lang: string): Observable<Object> {
//     return this.http
//       .get(`${this.path}${lang}.json`, { responseType: 'text' })
//       .pipe(map(data => parse(data)));
//   }
// }

// // Server-side loader
// @Injectable()
// export class TranslateServerLoader implements TranslateLoader {
//   constructor(@Inject(PLATFORM_ID) private readonly platformId: Object) {}

//   getTranslation(lang: string): Observable<any> {
//     const path = join(__dirname, '../browser/assets/i18n/', `${lang}.json`);
//     try {
//       const content = readFileSync(path, 'utf-8');
//       return of(JSON.parse(content));
//     } catch (error) {
//       console.error(`Translation file not found: ${path}`, error);
//       return of({});
//     }
//   }
// }

// Factory for both browser/server
export function translationLoaderFactory(httpClient: HttpClient, platformId: Object): TranslateLoader {
  if (isPlatformServer(platformId)) {
    const { join } = require('path');
    const { readFileSync } = require('fs');

    class ServerLoader implements TranslateLoader {
      getTranslation(lang: string): Observable<any> {
        const path = join(__dirname, '../browser/assets/i18n', `${lang}.json`);
        const content = readFileSync(path, 'utf-8');
        return of(JSON.parse(content));
      }
    }

    return new ServerLoader();
  } else {
    const { TranslateHttpLoader } = require('@ngx-translate/http-loader');
    return new TranslateHttpLoader(httpClient, '/assets/i18n/', '.json');
  }
}
