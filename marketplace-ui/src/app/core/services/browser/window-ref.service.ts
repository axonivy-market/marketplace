import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class WindowRef {
  private readonly platformId = inject(PLATFORM_ID);

  get nativeWindow(): Window | undefined {
    if (isPlatformBrowser(this.platformId)) {
      return window;
    } else {
      return undefined;
    }
  }
}
