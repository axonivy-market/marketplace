import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, inject, signal, ViewChild } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageSelectionComponent } from '../language-selection/language-selection.component';
import { ThemeSelectionComponent } from '../theme-selection/theme-selection.component';
import { LanguageService } from '../../../../core/services/language/language.service';
import { SEARCH_URL } from '../../../constants/common.constant';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    ThemeSelectionComponent,
    FormsModule,
    LanguageSelectionComponent,
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
  searchUrl = SEARCH_URL;
  isSearchBarDisplayed = signal(false);
  isGoogleSearchBarDisplayed = signal(false);

  translateService = inject(TranslateService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  isGoogleLoaded = false;
  inputValue: string = '';

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isGoogleSearchBarDisplayed.set(false);
      this.isSearchBarDisplayed.set(false);
    }
  }

  onClickSearchIcon() {
    this.isGoogleSearchBarDisplayed.set(true);
    this.isSearchBarDisplayed.set(true);
  }

  onHideSearch() {
    this.isGoogleSearchBarDisplayed.set(false);
    this.isSearchBarDisplayed.set(false);
  }
}
