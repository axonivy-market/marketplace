// src/app/shared/services/bootstrap-loader.service.ts
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class BootstrapLoaderService {
  private readonly platformId = inject(PLATFORM_ID);

  async init(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      // Import Bootstrap JS or other DOM-dependent logic
      await import('@ng-bootstrap/ng-bootstrap'); // or jQuery plugins
      // (Optional) also call enableDismissTrigger() if needed
    }
  }
}
