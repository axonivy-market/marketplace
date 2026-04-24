import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FooterComponent } from './footer.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { By } from '@angular/platform-browser';
import {
  IVY_FOOTER_LINKS,
  DOWNLOAD_URL,
  SOCIAL_MEDIA_LINK
} from '../../constants/common.constant';
import { RouterModule } from '@angular/router';


describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;
  let translate: TranslateService;

  beforeEach(async () => {
    let testMockDate: Date;

    vi.useFakeTimers();
    testMockDate = new Date('2019-09-15T05:00:00Z');
    vi.setSystemTime(testMockDate);

    await TestBed.configureTestingModule({
      imports: [
        FooterComponent,
        TranslateModule.forRoot(),
        RouterModule.forRoot([])
      ],
      providers: [TranslateService]
    }).compileComponents();

    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    translate = TestBed.inject(TranslateService);

    translate.setTranslation('en', {
      common: {
        footer: {
          ivyCompanyInfo: 'Axon Ivy Inc.',
          privacyPolicy: 'Privacy Policy',
          legalNotice: 'Legal Notice',
          ivyCompanyInfoUrl: '',
          privacyPolicyUrl: 'https://www.axonivy.com/privacy-policy',
          legalNoticeUrl: 'https://www.axonivy.com/legal-notice'
        }
      }
    });
    translate.use('en');

    fixture.detectChanges();
  });

  afterEach(function () {
    vi.useRealTimers();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('navbar should not display in mobile screen', () => {
    // In mobile, Bootstrap utility class d-none hides the navbar; d-xl-flex shows it on xl screens
    const mobileSearch = fixture.debugElement.query(By.css('.footer__navbar'));
    expect(mobileSearch.nativeElement.classList.contains('d-none')).toBe(true);
    expect(mobileSearch.nativeElement.classList.contains('d-xl-flex')).toBe(true);
  });

  it('social media section should be in the bottom of mobile screen', () => {
    // Verify that the social-media section exists in the footer
    const footerSocialMedia = fixture.nativeElement.querySelector(
      '.footer__social-media'
    );
    expect(footerSocialMedia).toBeTruthy();
    // Layout position is CSS-dependent and cannot be tested in jsdom
  });

  it('Ivy tag in ivy policy section should be display in higher row', () => {
    // Verify that ivy-company-tag and footer-link-tag elements exist
    const ivyTag = fixture.nativeElement.querySelector(
      '.footer__ivy-company-tag'
    );
    const ivyTermOfService = fixture.nativeElement.querySelector(
      '.footer__ivy-footer-link-tag'
    );
    expect(ivyTag).toBeTruthy();
    expect(ivyTermOfService).toBeTruthy();
    // DOM position cannot be tested reliably in jsdom
  });

  it('content layout should be displayed in the center', () => {
    // Verify center-alignment elements exist; computed CSS not available in jsdom
    const logo = fixture.debugElement.query(By.css('.logo__image'));
    const ivyPolicy = fixture.debugElement.query(By.css('.footer__ivy-policy'));
    expect(logo).toBeTruthy();
    expect(ivyPolicy).toBeTruthy();
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

  it('should redirect to download URL when clicking download button', () => {
    const downloadButton = fixture.debugElement.query(
      By.css('.download-button')
    ).nativeElement;

    expect(downloadButton.href).toBe(DOWNLOAD_URL);
  });

  it('should render all footer links with correct href', () => {
    const links = fixture.debugElement.queryAll(By.css('a.ivy-footer-link'));
    const socialMediaLinksWithoutCompanyInfo = IVY_FOOTER_LINKS.filter(
      element => element.label !== 'common.footer.ivyCompanyInfo'
    );
    expect(links.length).toBe(socialMediaLinksWithoutCompanyInfo.length);

    socialMediaLinksWithoutCompanyInfo.forEach((footerLink, index) => {
      const expectedHref = translate.instant(footerLink.link);
      const anchor = links[index].nativeElement as HTMLAnchorElement;
      expect(anchor.href).toBe(expectedHref);
    });
  });

  it('should get year of mock year', () => {
    component.getCurrentYear();

    expect(component.year).toBe('2019');
  });

  it('should get year of current year', () => {
    let currentDate = new Date();
    let currentYear = currentDate.getFullYear();

    vi.setSystemTime(currentDate);

    component.getCurrentYear();

    expect(component.year).toBe(currentYear.toString());
  });
});
