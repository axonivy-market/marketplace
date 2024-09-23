import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ERROR_PAGE_PATH } from '../../constants/common.constant';

@Component({
  selector: 'app-error-page-component',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './error-page-component.component.html',
  styleUrl: './error-page-component.component.scss'
})
export class ErrorPageComponentComponent implements OnInit {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isMobileMode = signal<boolean>(false);
  route = inject(ActivatedRoute);

  errorId: string | undefined;

  constructor(private readonly router: Router) {
    this.checkMediaSize();
  }

  ngOnInit(): void {
    this.errorId = this.route.snapshot.params['id'];
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
    if (this.isMobileMode()) {
      return '/assets/images/misc/robot-mobile.png';
    }

    return '/assets/images/misc/robot.png';
  }

  getImageSrcInDarkMode(): string {
    if (this.isMobileMode()) {
      return '/assets/images/misc/robot-mobile-black.png';
    }

    return '/assets/images/misc/robot-black.png';
  }
}
