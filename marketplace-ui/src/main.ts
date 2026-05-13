/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

if (typeof globalThis.window !== 'undefined' && typeof globalThis.document !== 'undefined') {
  void (async () => {
    try {
      await import('bootstrap/dist/js/bootstrap.bundle.min.js');
    } catch {
      // Ignore optional UI script loading failures during app startup.
    }
  })();
}

bootstrapApplication(AppComponent, appConfig).catch(err => {
  throw err;
});