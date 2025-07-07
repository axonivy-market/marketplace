import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class DocumentRef {
  private platformId = inject(PLATFORM_ID);

  get nativeDocument(): Document | undefined {
    return isPlatformBrowser(this.platformId) ? document : undefined;
  }
}
