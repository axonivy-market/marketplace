import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import {
  DOWNLOAD_URL,
  IVY_FOOTER_LINKS,
  NAV_ITEMS,
  SOCIAL_MEDIA_LINK
} from '../../constants/common.constant';
import { NavItem } from '../../models/nav-item.model';
import { LanguageService } from '../../../core/services/language/language.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss', '../../../app.component.scss']
})
export class FooterComponent implements OnInit {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  socialMediaLinks = SOCIAL_MEDIA_LINK;
  navItems: NavItem[] = NAV_ITEMS;
  ivyFooterLinks = IVY_FOOTER_LINKS;
  downloadUrl = DOWNLOAD_URL;
  year = '';
  isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    if (this.isBrowser) {
      this.getCurrentYear();
    }
  }

  getCurrentYear() {
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    this.year = currentYear.toString();
  }
}
