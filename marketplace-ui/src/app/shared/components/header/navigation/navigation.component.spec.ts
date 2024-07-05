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

  it('mobile search should display in small screen', () => {
    viewport.set(540);

    const mobileSearch = fixture.debugElement.query(
      By.css('.header-mobile__search')
    );

    expect(getComputedStyle(mobileSearch.nativeElement).display).not.toBe(
      'none'
    );
  });

  it('mobile search should not display in large screen', () => {
    viewport.set(1920);

    const mobileSearch = fixture.debugElement.query(
      By.css('.header-mobile__search')
    );

    expect(getComputedStyle(mobileSearch.nativeElement).display).toBe('none');
  });
});
