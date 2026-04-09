import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ThemeSelectionComponent } from '../theme-selection/theme-selection.component';
import { FormsModule } from '@angular/forms';
import { LanguageSelectionComponent } from '../language-selection/language-selection.component';
import { GithubUserBadgeComponent } from '../../github-user-badge/github-user-badge.component';
import { SEARCH_URL } from '../../../constants/common.constant';
import { LanguageService } from '../../../../core/services/language/language.service';
import { AdminAuthService } from '../../../../modules/admin-dashboard/admin-auth.service';

@Component({
  selector: 'app-header-toolbar',
  imports: [
    CommonModule,
    TranslateModule,
    ThemeSelectionComponent,
    FormsModule,
    LanguageSelectionComponent,
    GithubUserBadgeComponent
  ],
  templateUrl: './header-toolbar.component.html',
  styleUrl: './header-toolbar.component.scss'
})
export class HeaderToolbarComponent {
  isCollapsed = false;
  searchUrl = SEARCH_URL;
  isGoogleSearchBarDisplayed = signal(false);

  translateService = inject(TranslateService);
  elementRef = inject(ElementRef);
  languageService = inject(LanguageService);
  adminAuthService = inject(AdminAuthService);
  isGoogleLoaded = false;
  
  userInfo = this.adminAuthService.userInfo;

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isGoogleSearchBarDisplayed.set(false);
    }
  }

  onClickSearchIcon() {
    this.isGoogleSearchBarDisplayed.set(true);
  }

  onHideSearch() {
    this.isGoogleSearchBarDisplayed.set(false);
  }
}
