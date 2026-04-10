import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  Output,
  Renderer2,
  TemplateRef,
  inject,
  model
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { NgbOffcanvas, NgbOffcanvasRef } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { HeaderOffcanvasService } from '../../services/header-offcanvas.service';
import { GithubUserBadgeComponent } from '../github-user-badge/github-user-badge.component';
import { HeaderToolbarComponent } from './header-toolbar/header-toolbar.component';
import { NavigationComponent } from './navigation/navigation.component';

@Component({
  selector: 'app-header',
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    NavigationComponent,
    HeaderToolbarComponent,
    RouterLink,
    GithubUserBadgeComponent
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss', '../../../app.component.scss']
})
export class HeaderComponent {
  offcanvasService = inject(NgbOffcanvas);
  adminAuthService = inject(AdminAuthService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  headerOffcanvasService = inject(HeaderOffcanvasService);
  private readonly router = inject(Router);

  selectedNav = '/';
  isAdminRoute = false;
  userInfo = this.adminAuthService.userInfo;

  constructor(private readonly renderer: Renderer2) {
    this.translateService.setDefaultLang(
      this.languageService.selectedLanguage()
    );
    this.translateService.use(this.languageService.selectedLanguage());

    this.updateAdminState(this.router.url);
    this.router.events
      .pipe(
        filter(
          (event): event is NavigationEnd => event instanceof NavigationEnd
        )
      )
      .subscribe(event => {
        const url = event.urlAfterRedirects ?? event.url;
        this.updateAdminState(url);
      });
  }

  @HostListener('window:resize', [])
  onResize() {
    this.headerOffcanvasService.handleResize();
  }

  private updateAdminState(url: string): void {
    this.isAdminRoute = url.startsWith('/internal-dashboard');
  }

  toggleHeaderOffcanvas(content: TemplateRef<any>) {
    this.headerOffcanvasService.toggle(content, this.renderer);
  }

  isHeaderOffcanvasOpen(): boolean {
    return this.headerOffcanvasService.isOpen();
  }
}
