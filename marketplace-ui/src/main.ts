/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

(async () => {
  if (window && document) {
    try {
      await import('bootstrap/dist/js/bootstrap.bundle.min.js');
    } catch {
      // Ignore optional UI script loading failures during app startup.
    }
  }
  await bootstrapApplication(AppComponent, appConfig);
})();