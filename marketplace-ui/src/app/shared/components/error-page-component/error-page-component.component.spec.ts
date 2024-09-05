import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorPageComponentComponent } from './error-page-component.component';
import { TranslateModule } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
declare const viewport: Viewport;

describe('ErrorPageComponentComponent', () => {
  let component: ErrorPageComponentComponent;
  let fixture: ComponentFixture<ErrorPageComponentComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorPageComponentComponent, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorPageComponentComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call checkMediaSize on window resize', () => {
    spyOn(component, 'checkMediaSize');
    component.onResize();
    expect(component.checkMediaSize).toHaveBeenCalled();
  });

  it('should display image with the light mode on small and large viewport', () => {
    component.themeService.isDarkMode.set(false);
    viewport.set(1920);
    component.onResize();
    fixture.detectChanges();
    let imgElement = fixture.debugElement.query(By.css('img'));
    expect(imgElement.attributes['src']).toBe('/assets/images/misc/robot.png');

    viewport.set(540);
    component.onResize();
    fixture.detectChanges();
    expect(imgElement.attributes['src']).toBe(
      '/assets/images/misc/robot-mobile.png'
    );
  });

  it('should display image with the dark mode on small and large viewport', () => {
    component.themeService.isDarkMode.set(true);
    viewport.set(1920);
    component.onResize();
    fixture.detectChanges();
    let imgElement = fixture.debugElement.query(By.css('img'));
    expect(imgElement.attributes['src']).toBe(
      '/assets/images/misc/robot-black.png'
    );

    viewport.set(540);
    component.onResize();
    fixture.detectChanges();
    expect(imgElement.attributes['src']).toBe(
      '/assets/images/misc/robot-mobile-black.png'
    );
  });

  it('should back to the home page', () => {
    spyOn(component, 'backToHomePage');
    let buttonElement = fixture.debugElement.query(By.css('button'));
    buttonElement.nativeElement.click();
    expect(component.backToHomePage).toHaveBeenCalled();
  });

  it('should redirect to the home page', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.backToHomePage();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});
