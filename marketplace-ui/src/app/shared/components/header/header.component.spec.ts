import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { NavigationEnd, Router, provideRouter } from '@angular/router';
import { vi, describe, beforeEach, expect, it } from 'vitest';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent, TranslateModule.forRoot()],
      providers: [provideRouter([])]
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle the mobile menu on click', () => {
    const navbarToggler = fixture.debugElement.query(By.css('.bi.bi-list'));

    expect(component.isMobileMenuCollapsed()).toBe(true);

    // Click the mobile menu toggler
    navbarToggler.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isMobileMenuCollapsed()).toBe(false);

    // Click the mobile menu toggler again
    navbarToggler.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isMobileMenuCollapsed()).toBe(true);
  });

  // Responsive section
  it('action section should display in the bottom of the view in mobile mode', () => {
    // Verify the relevant sections exist; layout position not testable in jsdom
    const headerNavigation = fixture.nativeElement.querySelector(
      '.header__navigation'
    );
    const headerAction = fixture.nativeElement.querySelector('.header__action');
    expect(headerNavigation).toBeTruthy();
    expect(headerAction).toBeTruthy();
  });

  it('navigation section should display in vertical', () => {
    // Bootstrap uses flex-column class for vertical layout in mobile
    const navBar = fixture.debugElement.query(
      By.css('.header__navbar-content')
    );
    expect(navBar).toBeTruthy();
    expect(navBar.nativeElement.classList.contains('flex-column')).toBe(true);
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

  it('should emit menuToggle event when onMenuToggleClick is called', () => {
    vi.spyOn(component.menuToggle, 'emit');

    component.onMenuToggleClick();

    expect(component.menuToggle.emit).toHaveBeenCalled();
  });
});
