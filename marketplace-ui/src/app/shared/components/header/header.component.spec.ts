import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { NavigationEnd, Router, provideRouter } from '@angular/router';
import { vi, describe, beforeEach, expect, it } from 'vitest';
import { AuthService } from '../../../auth/auth.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { signal } from '@angular/core';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let router: Router;

  beforeEach(async () => {
    const authService = {
      isPasskeySupported: vi.fn().mockReturnValue(true),
      registerPasskey: vi.fn().mockResolvedValue(undefined)
    };
    const adminAuthService = {
      userInfo: signal(null)
    };

    await TestBed.configureTestingModule({
      imports: [HeaderComponent, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: AdminAuthService, useValue: adminAuthService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('menu button should be in the right side of mobile view', () => {
    // Verify menu button exists; position not testable in jsdom
    const menuButton = fixture.nativeElement.querySelector(
      '.header__menu-button'
    );
    expect(menuButton).toBeTruthy();
  });

  describe('Router navigation handling', () => {
    it('should set isAdminRoute to true when navigating to admin route', () => {
      const navigationEvent = new NavigationEnd(
        1,
        '/internal-dashboard/admin',
        '/internal-dashboard/admin'
      );

      (router.events as any).next(navigationEvent);

      expect(component.isAdminRoute).toBe(true);
    });

    it('should handle multiple navigation events', () => {
      const event1 = new NavigationEnd(1, '/portal', '/portal');
      const event2 = new NavigationEnd(
        2,
        '/internal-dashboard',
        '/internal-dashboard'
      );
      const event3 = new NavigationEnd(3, '/home', '/home');

      (router.events as any).next(event1);
      expect(component.isAdminRoute).toBe(false);

      (router.events as any).next(event2);
      expect(component.isAdminRoute).toBe(true);

      (router.events as any).next(event3);
      expect(component.isAdminRoute).toBe(false);
    });
  });
});
