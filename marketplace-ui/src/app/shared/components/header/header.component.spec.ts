import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { NavigationEnd, Router } from '@angular/router';
import { provideRouter } from '@angular/router';

declare const viewport: Viewport;

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

    expect(component.isMobileMenuCollapsed()).toBeTrue();

    // Click the mobile menu toggler
    navbarToggler.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isMobileMenuCollapsed()).toBeFalse();

    // Click the mobile menu toggler again
    navbarToggler.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isMobileMenuCollapsed()).toBeTrue();
  });

  // Responsive section
  it('action section should display in the bottom of the view in mobile mode', () => {
    viewport.set(540);

    const headerNavigation = fixture.nativeElement.querySelector(
      '.header__navigation'
    );
    const headerAction = fixture.nativeElement.querySelector('.header__action');

    const headerNavigationBeforeShowNavBar =
      headerNavigation.getBoundingClientRect();
    const headerActionBeforeShowNavBar = headerAction.getBoundingClientRect();

    const menuButton = fixture.debugElement.query(
      By.css('.header__menu-button')
    );
    menuButton.triggerEventHandler('click', null);
    fixture.detectChanges();
    const headerNavigationAfterShowNavBar =
      headerNavigation.getBoundingClientRect();
    const headerActionAfterShowNavBar = headerAction.getBoundingClientRect();
    expect(headerNavigationBeforeShowNavBar.top).toBeLessThan(
      headerActionAfterShowNavBar.top
    );
    expect(headerActionBeforeShowNavBar.top).toBeLessThan(
      headerNavigationAfterShowNavBar.top
    );

    expect(headerNavigationAfterShowNavBar.bottom).toBeLessThan(
      headerActionAfterShowNavBar.top
    );
  });

  it('navigation section should display in vertical', () => {
    viewport.set(540);
    const menuButton = fixture.debugElement.query(
      By.css('.header__menu-button')
    );
    menuButton.triggerEventHandler('click', null);

    fixture.detectChanges();
    const navBar = fixture.debugElement.query(
      By.css('.header__navbar-content')
    );

    expect(getComputedStyle(navBar.nativeElement).flexDirection).toBe('column');
  });

  it('menu button should be in the right side of mobile view', () => {
    viewport.set(540);
    const menuButton = fixture.nativeElement.querySelector(
      '.header__menu-button'
    );

    const logo = fixture.nativeElement.querySelector('.logo__image');
    expect(menuButton.getBoundingClientRect().left).toBeGreaterThan(
      logo.getBoundingClientRect().right
    );
  });

  describe('Router navigation handling', () => {
    it('should set isAdminRoute to true when navigating to admin route', (done) => {
      const navigationEvent = new NavigationEnd(
        1,
        '/internal-dashboard/admin',
        '/internal-dashboard/admin'
      );

      setTimeout(() => {
        expect(component.isAdminRoute).toBeTrue();
        done();
      }, 10);

      (router.events as any).next(navigationEvent);
    });

    it('should set isAdminRoute to false when navigating to non-admin route', (done) => {
      component.isAdminRoute = true;

      const navigationEvent = new NavigationEnd(1, '/portal', '/portal');

      setTimeout(() => {
        expect(component.isAdminRoute).toBeFalse();
        done();
      }, 10);

      (router.events as any).next(navigationEvent);
    });

    it('should use urlAfterRedirects when available', (done) => {
      const navigationEvent = new NavigationEnd(
        1,
        '/some-url',
        '/internal-dashboard/redirected'
      );

      setTimeout(() => {
        expect(component.isAdminRoute).toBeTrue();
        done();
      }, 10);

      (router.events as any).next(navigationEvent);
    });

    it('should handle multiple navigation events', (done) => {
      const event1 = new NavigationEnd(1, '/portal', '/portal');
      const event2 = new NavigationEnd(2, '/internal-dashboard', '/internal-dashboard');
      const event3 = new NavigationEnd(3, '/home', '/home');

      (router.events as any).next(event1);
      
      setTimeout(() => {
        expect(component.isAdminRoute).toBeFalse();

        (router.events as any).next(event2);
        
        setTimeout(() => {
          expect(component.isAdminRoute).toBeTrue();

          (router.events as any).next(event3);
          
          setTimeout(() => {
            expect(component.isAdminRoute).toBeFalse();
            done();
          }, 10);
        }, 10);
      }, 10);
    });
  });

  it('should emit menuToggle event when onMenuToggleClick is called', () => {
    spyOn(component.menuToggle, 'emit');

    component.onMenuToggleClick();

    expect(component.menuToggle.emit).toHaveBeenCalled();
  });
});
