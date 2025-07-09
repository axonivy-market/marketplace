import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, inject, model, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageSelectionComponent } from './language-selection/language-selection.component';
import { NavigationComponent } from './navigation/navigation.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { ThemeSelectionComponent } from './theme-selection/theme-selection.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    NavigationComponent,
    ThemeSelectionComponent,
    LanguageSelectionComponent,
    SearchBarComponent,
    RouterLink
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss', '../../../app.component.scss']
})
export class HeaderComponent {
  selectedNav = '/';

  isMobileMenuCollapsed = model<boolean>(true);

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    if (this.isBrowser) {
      this.translateService.setDefaultLang(
        this.languageService.selectedLanguage()
      );
      this.translateService.use(this.languageService.selectedLanguage());
    }
  }

  onCollapsedMobileMenu() {
    this.isMobileMenuCollapsed.update(value => !value);
  }
}
