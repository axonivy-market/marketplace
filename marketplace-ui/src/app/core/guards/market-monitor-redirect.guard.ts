import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { WindowRef } from '../services/browser/window-ref.service';
import { MARKET_MONITOR_URL } from '../../shared/constants/common.constant';

export const marketMonitorRedirectGuard: CanActivateFn = () => {
  inject(WindowRef).nativeWindow?.location.replace(MARKET_MONITOR_URL);
  return false;
};
