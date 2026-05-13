/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

if (typeof window !== 'undefined' && typeof document !== 'undefined') {
  import('bootstrap/dist/js/bootstrap.bundle.min.js').catch(() => {
    // Ignore optional UI script loading failures during app startup.
  });
}

bootstrapApplication(AppComponent, appConfig).catch(err => {
  throw err;
});