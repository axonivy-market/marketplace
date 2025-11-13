import { HttpClient, provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom, PLATFORM_ID, provideZoneChangeDetection, TransferState, inject } from '@angular/core';
import { InMemoryScrollingFeature, InMemoryScrollingOptions, provideRouter, withInMemoryScrolling } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { routes } from './app.routes';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { provideMatomo, withRouter } from 'ngx-matomo-client';
import { BootstrapLoaderService } from './core/services/browser/bootstrap-loader.service';
import { translateUniversalLoaderFactory } from './core/configs/translate-loader.factory';
import { Language } from './shared/enums/language.enum';
import { RuntimeConfigService } from './core/configs/runtime-config.service';
import { RUNTIME_CONFIG_KEYS } from './core/models/runtime-config';

const scrollConfig: InMemoryScrollingOptions = {
  scrollPositionRestoration: 'disabled',
  anchorScrolling: 'disabled'
};

const inMemoryScrollingFeature: InMemoryScrollingFeature =
  withInMemoryScrolling(scrollConfig);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, inMemoryScrollingFeature),
    provideHttpClient(withFetch(), withInterceptors([apiInterceptor])),
    provideMatomo(() => {
        const configService = inject(RuntimeConfigService);
        return {
          siteId: configService.get(RUNTIME_CONFIG_KEYS.MARKET_MATOMO_SITE_ID),
          trackerUrl: configService.get(RUNTIME_CONFIG_KEYS.MARKET_MATOMO_TRACKER_URL)
        };
      },
      withRouter()
    ),
    importProvidersFrom(
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useFactory: translateUniversalLoaderFactory,
          deps: [HttpClient, TransferState, PLATFORM_ID]
        },
        defaultLanguage: Language.EN
      })
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: (bootstrapLoader: BootstrapLoaderService) => () =>
        bootstrapLoader.init(),
      deps: [BootstrapLoaderService],
      multi: true
    }
  ]
};
