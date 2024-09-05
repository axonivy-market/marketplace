import { Component, HostListener, inject, signal } from '@angular/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-error-page-component',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './error-page-component.component.html',
  styleUrl: './error-page-component.component.scss'
})
export class ErrorPageComponentComponent {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isMobileMode = signal<boolean>(false);

  constructor(private readonly router: Router) {
    this.checkMediaSize();
  }

  backToHomePage() {
    this.router.navigate(['/']);
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    this.isMobileMode.set(mediaQuery.matches);
  }

  getImageSrcInLightMode(): string {
    let imageSrc = '';
    if (this.isMobileMode()) {
      imageSrc = '/assets/images/misc/robot-mobile.png';
    } else {
      imageSrc = '/assets/images/misc/robot.png';
    }

    return imageSrc;
  }

  getImageSrcInDarkMode(): string {
    let imageSrc = '';
    if (this.isMobileMode()) {
      imageSrc = '/assets/images/misc/robot-mobile-black.png';
    } else {
      imageSrc = '/assets/images/misc/robot-black.png';
    }

    return imageSrc;
  }
}
