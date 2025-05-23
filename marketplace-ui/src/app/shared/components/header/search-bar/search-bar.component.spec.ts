import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { SearchBarComponent } from './search-bar.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { ElementRef } from '@angular/core';

declare const viewport: Viewport;

describe('SearchBarComponent', () => {
  let component: SearchBarComponent;
  let fixture: ComponentFixture<SearchBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchBarComponent, TranslateModule.forRoot()],
      providers: [TranslateService,
        { provide: ElementRef, useValue: { nativeElement: document.createElement('div') } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('desktop search should not display in small screen', () => {
    viewport.set(540);

    const desktopSearch = fixture.debugElement.query(
      By.css('.header__search-button')
    );

    expect(desktopSearch).toBeNull;
  });

  it('desktop search should display in large screen', () => {
    viewport.set(1920);

    const desktopSearch = fixture.debugElement.query(
      By.css('.header__search-button')
    );

    expect(getComputedStyle(desktopSearch.nativeElement).display).not.toBe(
      'none'
    );
  });

  it('should set isGoogleSearchBarDisplayed to false when clicking outside', () => {
    // Set up the DOM
    const outsideClickEvent = new MouseEvent('click', {
      bubbles: true,
      cancelable: true,
      view: window
    });

    // Dispatch a click event to the document
    document.dispatchEvent(outsideClickEvent);

    // Verify the behavior
    expect(component.isGoogleSearchBarDisplayed()).toBeFalse();
  });

  it('should show the google search bar on search icon click', () => {
    viewport.set(1920);
    const searchIcon = fixture.debugElement.query(
      By.css('.header__search-button')
    );
    fixture.detectChanges();

    const googleSearchContainer = fixture.debugElement.query(
      By.css('.google-search-container')
    );

    expect(component.isGoogleSearchBarDisplayed()).toBeFalse();
    expect(googleSearchContainer).toBeTruthy();
    expect(getComputedStyle(googleSearchContainer.nativeElement).display).toBe('none');

    // Click the search icon
    searchIcon.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.isGoogleSearchBarDisplayed()).toBeTrue();
    expect(googleSearchContainer).toBeTruthy();
    expect(getComputedStyle(googleSearchContainer.nativeElement).display).toBe('block');
  });

  it('should set isGoogleSearchBarDisplayed to false when onHideSearch is called', () => {
    // Ensure the initial state is true
    component.isGoogleSearchBarDisplayed.set(true);
    expect(component.isGoogleSearchBarDisplayed()).toBeTrue();
  
    // Call the onHideSearch method
    component.onHideSearch();
  
    // Verify the state is set to false
    expect(component.isGoogleSearchBarDisplayed()).toBeFalse();
  });
});
