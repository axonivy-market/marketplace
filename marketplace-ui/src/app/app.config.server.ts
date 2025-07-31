import { mergeApplicationConfig, ApplicationConfig, importProvidersFrom, TransferState, PLATFORM_ID } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { translateUniversalLoaderFactory } from './core/configs/translate-loader.factory';
import { HttpClient, HttpRequest } from '@angular/common/http';
import { provideClientHydration, withEventReplay, withHttpTransferCacheOptions } from '@angular/platform-browser';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    importProvidersFrom(
          TranslateModule.forRoot({
            loader: {
              provide: TranslateLoader,
              useFactory: translateUniversalLoaderFactory,
              deps: [HttpClient, TransferState, PLATFORM_ID]
            }
          })
        )
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
