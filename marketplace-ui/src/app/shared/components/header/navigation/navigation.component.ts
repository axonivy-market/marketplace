import { CommonModule } from '@angular/common';
import { Component, inject, Input, signal, ChangeDetectorRef, NgZone, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ADMIN_NAV_ITEMS, NAV_ITEMS, SEARCH_URL } from '../../../constants/common.constant';
import { NavItem } from '../../../models/nav-item.model';
import { HeaderOffcanvasService } from '../../../services/header-offcanvas.service';

@Component({
  selector: 'app-navigation',
  imports: [CommonModule, TranslateModule, RouterLink, RouterLinkActive],
  templateUrl: './navigation.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent {
  @Input() navItems: NavItem[] = NAV_ITEMS;
  @Input() adminNavItems: NavItem[] = ADMIN_NAV_ITEMS;
  @Input() isAdminPage = false;
  @Input() isInHeaderOffCanvas = false;

  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  headerOffcanvasService = inject(HeaderOffcanvasService);
  cdr = inject(ChangeDetectorRef);
  ngZone = inject(NgZone);
  isMobileMode = signal<boolean>(false);
  searchUrl = SEARCH_URL;

  get items(): NavItem[] {
    return this.isAdminPage ? this.adminNavItems : this.navItems;
  }

  closeHeaderOffcanvas() {
    this.ngZone.run(() => {
      this.headerOffcanvasService.close();
      this.cdr.markForCheck();
    });
  }
}
