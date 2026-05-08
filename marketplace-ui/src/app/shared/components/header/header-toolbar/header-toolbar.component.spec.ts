import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElementRef } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HeaderToolbarComponent } from './header-toolbar.component';
import { By } from '@angular/platform-browser';

describe('HeaderToolbarComponent', () => {
  let component: HeaderToolbarComponent;
  let fixture: ComponentFixture<HeaderToolbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderToolbarComponent, TranslateModule.forRoot()],
      providers: [
        TranslateService,
        {
          provide: ElementRef,
          useValue: { nativeElement: document.createElement('div') }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set isGoogleSearchBarDisplayed to false when clicking outside', () => {
    const outsideClickEvent = new MouseEvent('click', {
      bubbles: true,
      cancelable: true,
      view: window
    });

    document.dispatchEvent(outsideClickEvent);
    expect(component.isGoogleSearchBarDisplayed()).toBe(false);
  });

  it('should show the google search bar on search icon click', () => {
    const searchIcon = fixture.debugElement.query(
      By.css('.header__search-button')
    );
    fixture.detectChanges();

    const googleSearchContainer = fixture.debugElement.query(
      By.css('.google-search-container')
    );

    expect(component.isGoogleSearchBarDisplayed()).toBe(false);
    expect(googleSearchContainer).toBeTruthy();
    expect(getComputedStyle(googleSearchContainer.nativeElement).display).toBe(
      'none'
    );
    searchIcon.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isGoogleSearchBarDisplayed()).toBe(true);
    expect(googleSearchContainer).toBeTruthy();
    expect(getComputedStyle(googleSearchContainer.nativeElement).display).toBe(
      'block'
    );
  });

  it('should set isGoogleSearchBarDisplayed to false when onHideSearch is called', () => {
    component.isGoogleSearchBarDisplayed.set(true);
    expect(component.isGoogleSearchBarDisplayed()).toBe(true);

    component.onHideSearch();

    expect(component.isGoogleSearchBarDisplayed()).toBe(false);
  });
});
