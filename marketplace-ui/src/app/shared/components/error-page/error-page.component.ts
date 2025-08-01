import {
  Component,
  HostListener,
  Inject,
  inject,
  OnInit,
  PLATFORM_ID,
  signal
} from '@angular/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { I18N_DEFAULT_ERROR_CODE, I18N_ERROR_CODE_PATH } from '../../constants/common.constant';

@Component({
  selector: 'app-error-page-component',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './error-page.component.html',
  styleUrl: './error-page.component.scss'
})
export class ErrorPageComponent implements OnInit {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  isMobileMode = signal<boolean>(false);
  route = inject(ActivatedRoute);
  errorMessageKey = '';
  errorId: string | undefined;

  constructor(
    private readonly router: Router,
    @Inject(PLATFORM_ID) private readonly platformId: Object
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.checkMediaSize();
    }
  }

  ngOnInit(): void {
    this.errorId = this.route.snapshot.params['id'];
    
    this.translateService
      .get(I18N_ERROR_CODE_PATH)
      .subscribe(errorTranslations => {
        let i18nErrorKey = this.errorId;
        if (
          !i18nErrorKey ||
          !Object.keys(errorTranslations).includes(i18nErrorKey)
        ) {
          i18nErrorKey = I18N_DEFAULT_ERROR_CODE;
        }
        this.errorMessageKey = this.buildI18nKey(i18nErrorKey);
      });
  }

  private buildI18nKey(key: string | undefined) {
    return `${I18N_ERROR_CODE_PATH}.${key}`;
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
