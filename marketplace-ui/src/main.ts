/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { SecurityMonitorComponent } from './app/modules/security-monitor/security-monitor.component';
import { provideHttpClient } from '@angular/common/http';

const currentPath = window.location.pathname;
if (currentPath.startsWith('/security-monitor')) {
  bootstrapApplication(SecurityMonitorComponent, {
    providers: [provideHttpClient()]
  }).catch(err => console.error(err));
} else {
  bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
}
