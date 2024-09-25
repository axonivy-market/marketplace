import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FooterComponent } from './footer.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { IVY_FOOTER_LINKS, SOCIAL_MEDIA_LINK } from '../../constants/common.constant';

declare const viewport: Viewport;

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async () => {
    let testMockDate: Date;

    jasmine.clock().uninstall();
    jasmine.clock().install();
    testMockDate = new Date('2019-09-15T05:00:00Z');
    jasmine.clock().mockDate(testMockDate);

    await TestBed.configureTestingModule({
      imports: [FooterComponent, TranslateModule.forRoot()],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
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

  it('should navigate to the correct URL when the social link icon is clicked', () => {
    const socialMediaLinks = fixture.debugElement.queryAll(
      By.css('.social-link')
    );

    for (let index = 0; index < socialMediaLinks.length; index++) {
      const socialMediaLinkElement: HTMLAnchorElement =
      socialMediaLinks[index].nativeElement;

      socialMediaLinkElement.click();

      expect(socialMediaLinkElement.href).toBe(SOCIAL_MEDIA_LINK[index].url);
    }
  });

  it('should navigate to the correct URL when the policy link icon is clicked', () => {
    const policyLinks = fixture.debugElement.queryAll(
      By.css('.policy-link')
    );

    for (let index = 0; index < policyLinks.length; index++) {
      const policyLinkElement: HTMLAnchorElement =
      policyLinks[index].nativeElement;

      policyLinkElement.click();

      expect(policyLinkElement.href).toBe(IVY_FOOTER_LINKS[index].link);
    }
  });

  it('should get year of mock year', () => {
    component.getCurrentYear();

    expect(component.year).toBe('2019');
  })  

  it('should get year of current year', () => {
    let currentDate = new Date();
    let currentYear = currentDate.getFullYear();

    jasmine.clock().mockDate(currentDate);

    component.getCurrentYear();

    expect(component.year).toBe(currentYear.toString());
  })
});
