import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
  WritableSignal,
  inject,
  model,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { NavigationComponent } from './navigation/navigation.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { filter } from 'rxjs';
import { GithubUserBadgeComponent } from '../github-user-badge/github-user-badge.component';
import {
  NgbDatepicker,
  NgbInputDatepicker,
  NgbOffcanvas,
  NgbOffcanvasRef,
  OffcanvasDismissReasons
} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-header',
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    NavigationComponent,
    SearchBarComponent,
    RouterLink,
    GithubUserBadgeComponent,
    NgbInputDatepicker
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss', '../../../app.component.scss']
})
export class HeaderComponent {
  offcanvasService = inject(NgbOffcanvas);
  headerOffcanvasRef: NgbOffcanvasRef | null = null;

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

  onCollapsedMobileMenu() {
    this.isMobileMenuCollapsed.update(value => !value);
  }

  onMenuToggleClick(): void {
    this.menuToggle.emit();
  }

  private updateAdminState(url: string): void {
    this.isAdminRoute = url.startsWith('/internal-dashboard');
  }

  toggleHeaderOffcanvas(content: TemplateRef<any>) {
    if (this.headerOffcanvasRef) {
      this.headerOffcanvasRef.close();
      return;
    }
    this.open(content);
  }

  open(content: TemplateRef<any>) {
    this.headerOffcanvasRef = this.offcanvasService.open(content, {
      ariaLabelledBy: 'offcanvas-basic-title',
      backdrop: true,
      panelClass: 'my-offcanvas',
      position: 'end',
      backdropClass: 'my-offcanvas-backdrop'
    });

    this.headerOffcanvasRef.result.finally(() => {
      this.headerOffcanvasRef = null;
    });
  }

  // private getDismissReason(reason: any): string {
  //   switch (reason) {
  //     case OffcanvasDismissReasons.ESC:
  //       return 'by pressing ESC';
  //     case OffcanvasDismissReasons.BACKDROP_CLICK:
  //       return 'by clicking on the backdrop';
  //     default:
  //       return `with: ${reason}`;
  //   }
  // }
}
