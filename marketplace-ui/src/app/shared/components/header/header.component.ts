import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject, model } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { NavigationComponent } from './navigation/navigation.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { filter } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    NavigationComponent,
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
  private readonly router = inject(Router);

  @Input() showNavigation = true;
  @Input() showMenuToggle = false;
  @Output() menuToggle = new EventEmitter<void>();

  isAdminRoute = false;

  constructor() {
    this.translateService.setDefaultLang(
      this.languageService.selectedLanguage()
    );
    this.translateService.use(this.languageService.selectedLanguage());

    this.updateAdminState(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => {
        const url = event.urlAfterRedirects ?? event.url;
        this.updateAdminState(url);
      });
  }

  onCollapsedMobileMenu() {
    this.isMobileMenuCollapsed.update(value => !value);
  }

  onMenuToggleClick(): void {
    this.menuToggle.emit();
  }

  get shouldShowNavigation(): boolean {
    return this.showNavigation && !this.isAdminRoute;
  }

  get shouldShowMenuToggle(): boolean {
    return this.showMenuToggle || this.isAdminRoute;
  }

  private updateAdminState(url: string): void {
    this.isAdminRoute = url.startsWith('/octopus');
  }
}
