import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class BootstrapLoaderService {
  private readonly platformId = inject(PLATFORM_ID);

  async init(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      await this.loadNgbModule();
    }
  }

  protected async loadNgbModule(): Promise<any> {
    return import('@ng-bootstrap/ng-bootstrap');
  }
}
