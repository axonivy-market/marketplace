import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NavigationComponent } from './navigation.component';

declare const viewport: Viewport;

describe('NavigationComponent', () => {
  let component: NavigationComponent;
  let fixture: ComponentFixture<NavigationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavigationComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(NavigationComponent);
    component = fixture.componentInstance;
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

  it('should display google search bar container in mobile mode', () => {
    component.isMobileMode.set(true); // Simulate mobile mode
    fixture.detectChanges();

    const mobileSearch = fixture.debugElement.query(
      By.css('.google-search-container')
    );
    expect(mobileSearch).toBeTruthy();
    expect(getComputedStyle(mobileSearch.nativeElement).display).toBe('block');
  });

  it('should hide google search bar container in desktop mode', () => {
    component.isMobileMode.set(false); // Simulate desktop mode
    fixture.detectChanges();

    const mobileSearch = fixture.debugElement.query(
      By.css('.google-search-container')
    );
    expect(mobileSearch).toBeTruthy();
    expect(getComputedStyle(mobileSearch.nativeElement).display).toBe('none');
  });
});
