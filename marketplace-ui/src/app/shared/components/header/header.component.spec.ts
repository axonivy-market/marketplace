import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';

declare const viewport: Viewport;

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

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
});
