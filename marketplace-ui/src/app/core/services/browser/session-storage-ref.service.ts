// src/app/shared/services/storage-ref.service.ts
import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class SessionStorageRef {
  private readonly platformId = inject(PLATFORM_ID);

  get session(): Storage | null {
    if (isPlatformBrowser(this.platformId)) {
      return sessionStorage;
    } else {
      return null;
    }
  }
}
