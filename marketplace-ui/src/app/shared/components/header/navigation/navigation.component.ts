import { CommonModule } from '@angular/common';
import { Component, HostListener, inject, Input, signal } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ADMIN_NAV_ITEMS, NAV_ITEMS, SEARCH_URL } from '../../../constants/common.constant';
import { NavItem } from '../../../models/nav-item.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { WindowRef } from '../../../../core/services/browser/window-ref.service';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, TranslateModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {
  @Input() navItems: NavItem[] = NAV_ITEMS;
  @Input() adminNavItems: NavItem[] = ADMIN_NAV_ITEMS;
  @Input() isAdmin = false;

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  isMobileMode = signal<boolean>(false);
  searchUrl = SEARCH_URL;

  constructor(private readonly windowRef: WindowRef) {
    this.checkMediaSize();
  }

  @HostListener('window:resize')
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const win = this.windowRef.nativeWindow;
    if (win) {
      const mediaQuery = win.matchMedia('(max-width: 992px)');
      this.isMobileMode.set(mediaQuery.matches);
    }
  }

  get items(): NavItem[] {
    return this.isAdmin ? this.adminNavItems : this.navItems;
  }
}
