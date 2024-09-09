import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Output,
  signal,
  WritableSignal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
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
  @Output() isHeaderMenuShown = new EventEmitter<boolean>();

  selectedNav = '/';

  isMobileMenuCollapsed: WritableSignal<boolean> = signal(true);

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);

  constructor() {
    this.translateService.setDefaultLang(
      this.languageService.selectedLanguage()
    );
    this.translateService.use(this.languageService.selectedLanguage());
  }

  onCollapsedMobileMenu() {
    this.isMobileMenuCollapsed.update(value => !value);
    this.isHeaderMenuShown.emit(this.isMobileMenuCollapsed());
  }
}
