import { CommonModule } from '@angular/common';
import { Component, WritableSignal, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Language } from '../../enums/language.enum';
import { LanguageSelectionComponent } from './language-selection/language-selection.component';
import { NavigationComponent } from './navigation/navigation.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { ThemeSelectionComponent } from './theme-selection/theme-selection.component';

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
    SearchBarComponent
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss', '../../../app.component.scss']
})
export class HeaderComponent {
  selectedNav = '/';
  selectedLanguage = Language.EN;

  isMobileMenuCollapsed: WritableSignal<boolean> = signal(true);

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);

  constructor() {
    this.selectedLanguage = this.languageService.getSelectedLanguage();
    this.translateService.setDefaultLang(this.selectedLanguage);
    this.translateService.use(this.selectedLanguage);
  }

  onCollapsedMobileMenu() {
    this.isMobileMenuCollapsed.update(value => !value);
  }
}
