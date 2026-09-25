import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { marketMonitorRedirectGuard } from './market-monitor-redirect.guard';
import { WindowRef } from '../services/browser/window-ref.service';
import { MARKET_MONITOR_URL } from '../../shared/constants/common.constant';

describe('marketMonitorRedirectGuard', () => {
  const runGuard = () =>
    TestBed.runInInjectionContext(() =>
      marketMonitorRedirectGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

  it('should redirect the browser to the market monitor and block activation', () => {
    const replace = vi.fn();
    TestBed.configureTestingModule({
      providers: [{ provide: WindowRef, useValue: { nativeWindow: { location: { replace } } } }]
    });

    expect(runGuard()).toBe(false);
    expect(replace).toHaveBeenCalledWith(MARKET_MONITOR_URL);
  });

  it('should block activation without redirecting when no browser window is available', () => {
    TestBed.configureTestingModule({
      providers: [{ provide: WindowRef, useValue: { nativeWindow: undefined } }]
    });

    expect(runGuard()).toBe(false);
  });
});
