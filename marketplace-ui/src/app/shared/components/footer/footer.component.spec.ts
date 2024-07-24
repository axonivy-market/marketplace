import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FooterComponent } from './footer.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';

declare const viewport: Viewport;

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FooterComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('navbar should not display in mobile screen', () => {
    viewport.set(540);

    const mobileSearch = fixture.debugElement.query(By.css('.footer__navbar'));

    expect(getComputedStyle(mobileSearch.nativeElement).display).toBe('none');
  });

  it('social media section should be in the bottom of mobile screen', () => {
    viewport.set(540);

    const footerSocialMedia = fixture.nativeElement.querySelector(
      '.footer__social-media'
    );
    const footerIvyPolicy = fixture.nativeElement.querySelector(
      '.footer__ivy-policy'
    );

    expect(footerSocialMedia.getBoundingClientRect().top).toBeGreaterThan(
      footerIvyPolicy.getBoundingClientRect().top
    );
  });

  it('Ivy tag in ivy policy section should be display in higher row', () => {
    viewport.set(540);

    const ivyTag = fixture.nativeElement.querySelector('.footer__ivy-tag');

    const ivyTermOfService = fixture.nativeElement.querySelector(
      '.footer__ivy-term-of-service-tag'
    );

    expect(ivyTag.getBoundingClientRect().top).toBeLessThan(
      ivyTermOfService.getBoundingClientRect().top
    );
  });

  it('content layout should be displayed in the center', () => {
    viewport.set(480);

    const logo = fixture.debugElement.query(By.css('.logo__image'));
    const ivyPolicy = fixture.debugElement.query(By.css('.footer__ivy-policy'));
    expect(getComputedStyle(logo.nativeElement).textAlign).toBe('center');
    expect(getComputedStyle(ivyPolicy.nativeElement).textAlign).toBe('center');
  });
});
