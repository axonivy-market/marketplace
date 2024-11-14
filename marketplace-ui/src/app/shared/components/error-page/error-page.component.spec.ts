import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorPageComponent } from './error-page.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
declare const viewport: Viewport;

describe('ErrorPageComponentComponent', () => {
  let component: ErrorPageComponent;
  let fixture: ComponentFixture<ErrorPageComponent>;
  let router: Router;

  const setupComponent = (idValue: string | undefined) => {
    TestBed.configureTestingModule({
      imports: [ErrorPageComponent, TranslateModule.forRoot()],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: idValue }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorPageComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  };

  it('should create', () => {
    setupComponent(undefined);
    expect(component).toBeTruthy();
  });

  it('should call checkMediaSize on window resize', () => {
    setupComponent(undefined);
    spyOn(component, 'checkMediaSize');
    component.onResize();
    expect(component.checkMediaSize).toHaveBeenCalled();
  });

  it('should display image with the light mode on small and large viewport', () => {
    setupComponent(undefined);
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
    setupComponent(undefined);
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
    setupComponent(undefined);
    spyOn(component, 'backToHomePage');
    let buttonElement = fixture.debugElement.query(By.css('button'));
    buttonElement.nativeElement.click();
    expect(component.backToHomePage).toHaveBeenCalled();
  });

  it('should redirect to the home page', () => {
    setupComponent(undefined);
    const navigateSpy = spyOn(router, 'navigate');
    component.backToHomePage();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  describe('when id is undefined', () => {
    beforeEach(() => {
      setupComponent(undefined);
    });

    it('should set error id to undefined', () => {
      const errorCode = fixture.debugElement.query(By.css('.error-code'));

      expect(component.errorId).toBeUndefined();
      expect(errorCode).toBeFalsy();
      expect(component.errorMessageKey).toEqual(
        'common.error.description.default'
      );
    });
  });

  describe('when error id is in error codes', () => {
    beforeEach(() => {
      setupComponent('404');
    });

    it('should set error id to the same as in route param', () => {
      const translateService = TestBed.inject(TranslateService);
      spyOn(translateService, 'get').and.returnValue(of({ '404': 'test' }));
      component.ngOnInit();
      fixture.detectChanges();
      const errorCode = fixture.debugElement.query(By.css('.error-code'));
      expect(component.errorId).toBe('404');
      expect(errorCode).toBeTruthy();
      expect(component.errorMessageKey).toEqual('common.error.description.404');
    });
  });
});