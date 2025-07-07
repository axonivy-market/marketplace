// src/app/shared/services/storage-ref.service.ts
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class SessionStorageRef {
  private platformId = inject(PLATFORM_ID);

  get session(): Storage | null {
    return isPlatformBrowser(this.platformId) ? sessionStorage : null;
  }
}
