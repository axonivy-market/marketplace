import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class WindowRef {
  private platformId = inject(PLATFORM_ID);

  get nativeWindow(): Window | undefined {
    return isPlatformBrowser(this.platformId) ? window : undefined;
  }
}