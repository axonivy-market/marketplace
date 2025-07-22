import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class DocumentRef {
  private readonly platformId = inject(PLATFORM_ID);

  get nativeDocument(): Document | undefined {
    if (isPlatformBrowser(this.platformId)) {
      return document;
    } else {
      return undefined;
    }
  }
}
