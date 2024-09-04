import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSelectionComponent } from '../language-selection/language-selection.component';
import { ThemeSelectionComponent } from '../theme-selection/theme-selection.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    ThemeSelectionComponent,
    LanguageSelectionComponent
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  isSearchBarDisplayed = signal(false);

  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isSearchBarDisplayed.set(false);
    }
  }

  onClickSearchIcon() {
    this.isSearchBarDisplayed.set(true);
  }

  onHideSearch() {
    this.isSearchBarDisplayed.set(false);
  }
}
