import { inject, Injectable, PLATFORM_ID, TransferState, Optional, Inject } from '@angular/core';
import { isPlatformBrowser, isPlatformServer } from '@angular/common';
import { environment } from '../../../environments/environment';
import { RUNTIME_CONFIG_KEY, RuntimeConfig } from '../models/runtime-config';

@Injectable({
  providedIn: 'root'
})
export class RuntimeConfigService {
  private readonly transferState = inject(TransferState);
  private readonly platformId = inject(PLATFORM_ID);
  private config: RuntimeConfig | null = null;

  constructor(
    @Optional() @Inject(RUNTIME_CONFIG_KEY) private readonly providedConfig: RuntimeConfig | null
  ) {
    // If config is provided via dependency injection (SSR), store it in TransferState
    if (providedConfig && isPlatformServer(this.platformId)) {
      this.transferState.set(RUNTIME_CONFIG_KEY, providedConfig);
      this.config = providedConfig;
    }
  }

  getConfig(): RuntimeConfig {
    if (!this.config) {
      this.config = environment as RuntimeConfig;
      if (isPlatformBrowser(this.platformId)) {
        this.config = this.transferState.get(RUNTIME_CONFIG_KEY, environment as RuntimeConfig);
      }
    }
    return this.config;
  }

  /**
   * Set the runtime configuration (SSR only), then stores in TransferState for client.
   */
  setConfig(config: RuntimeConfig): void {
    this.config = config;
    this.transferState.set(RUNTIME_CONFIG_KEY, config);
  }

  /**
   * Get a specific config value
   */
  get<K extends keyof RuntimeConfig>(key: K): RuntimeConfig[K] {
    return this.getConfig()[key];
  }
}
