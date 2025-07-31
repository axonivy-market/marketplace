// src/app/services/request-context.service.ts
import { Injectable, Inject, PLATFORM_ID, Optional } from '@angular/core';
import { isPlatformServer } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class RequestContextService {
  private serverRequest: any = null;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    // Try multiple possible token names
    @Optional() @Inject('REQUEST') private req1: any,
    @Optional() @Inject('request') private req2: any,
    @Optional() @Inject('SERVER_REQUEST') private req3: any,
    @Optional() @Inject('APP_BASE_HREF')  private req4: any,
  ) {
    if (isPlatformServer(this.platformId)) {
      this.serverRequest = this.req1 || this.req2 || this.req3 || this.req4;
      
      // Debug: Log what we found
      console.log('Request context found:', !!this.serverRequest);
      if (this.serverRequest) {
        console.log('Request URL:', this.serverRequest.url);
      }
    }
  }

  isServer(): boolean {
    return isPlatformServer(this.platformId);
  }

  getRequest(): any {
    return this.serverRequest;
  }

  getUrl(): string {
    return this.serverRequest?.url || '';
  }

  getHeader(name: string): string | null {
    if (!this.serverRequest) return null;
    
    // Try different ways to access headers
    return this.serverRequest.get?.(name) || 
           this.serverRequest.header?.(name) ||
           this.serverRequest.headers?.[name.toLowerCase()] ||
           null;
  }

  getAcceptLanguage(): string | null {
    return this.getHeader('Accept-Language');
  }

  getUserAgent(): string | null {
    return this.getHeader('User-Agent');
  }
}