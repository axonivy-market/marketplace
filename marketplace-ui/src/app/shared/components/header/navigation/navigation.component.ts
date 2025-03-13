import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, Input, Renderer2, signal } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { NAV_ITEMS, SEARCH_URL } from '../../../constants/common.constant';
import { NavItem } from '../../../models/nav-item.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { GoogleSearchComponentComponent } from "../../google-search-component/google-search-component.component";
import { GoogleSearchBarUtils } from '../../../utils/google-search-bar.utils';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, TranslateModule, GoogleSearchComponentComponent],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {
  @Input() navItems: NavItem[] = NAV_ITEMS;

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  isMobileMode = signal<boolean>(false);
  searchUrl = SEARCH_URL;

  constructor(private renderer: Renderer2) {
    this.checkMediaSize();
  }

  ngAfterViewInit(): void {
    GoogleSearchBarUtils.renderGoogleSearchBar(this.renderer);
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const mediaQuery = window.matchMedia('(max-width: 992px)');
    this.isMobileMode.set(mediaQuery.matches);
  }

  onClickSearchInput() {
    window.location.href = this.searchUrl;
  }
}
