import { HttpClient } from '@angular/common/http';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, map } from 'rxjs';
import { parse } from 'yaml';

class TranslateYamlHttpLoader implements TranslateLoader {
  constructor(
    private readonly http: HttpClient,
    public path = '/assets/i18n/'
  ) {}

  public getTranslation(lang: string): Observable<Object> {
    return this.http
      .get(`${this.path}${lang}.yaml`, { responseType: 'text' })
      .pipe(map(data => parse(data)));
  }
}

export function httpLoaderFactory(httpClient: HttpClient) {
  return new TranslateYamlHttpLoader(httpClient);
}
