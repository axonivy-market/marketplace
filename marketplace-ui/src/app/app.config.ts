import { HttpClient, provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig, importProvidersFrom, PLATFORM_ID, provideZoneChangeDetection, TransferState } from '@angular/core';
import { InMemoryScrollingFeature, InMemoryScrollingOptions, provideRouter, withInMemoryScrolling } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { routes } from './app.routes';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { provideMatomo, withRouter } from 'ngx-matomo-client';
import { environment } from '../environments/environment';
import { provideClientHydration, withI18nSupport } from '@angular/platform-browser';
import { BootstrapLoaderService } from './core/services/browser/bootstrap-loader.service';
import { translateUniversalLoaderFactory } from './core/configs/translate-loader.factory';
import { Language } from './shared/enums/language.enum';

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

    provideMatomo(
      {
        siteId: environment.matomoSiteId,
        trackerUrl: environment.matomoTrackerUrl
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
    provideClientHydration(withI18nSupport()),
    {
      provide: APP_INITIALIZER,
      useFactory: (bootstrapLoader: BootstrapLoaderService) => () =>
        bootstrapLoader.init(),
      deps: [BootstrapLoaderService],
      multi: true
    }
  ]
};
