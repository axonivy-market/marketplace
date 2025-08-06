import { mergeApplicationConfig, ApplicationConfig, importProvidersFrom, TransferState, PLATFORM_ID } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { translateUniversalLoaderFactory } from './core/configs/translate-loader.factory';
import { HttpClient } from '@angular/common/http';
import { Language } from './shared/enums/language.enum';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    importProvidersFrom(
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: translateUniversalLoaderFactory,
          deps: [HttpClient, TransferState, PLATFORM_ID]
        },
        defaultLanguage: Language.EN
      })
    )
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
