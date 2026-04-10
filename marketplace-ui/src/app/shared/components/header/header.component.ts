import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  Output,
  Renderer2,
  TemplateRef,
  WritableSignal,
  inject,
  model,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  NavigationEnd,
  Router,
  RouterLink,
  ɵEmptyOutletComponent
} from '@angular/router';
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
import { HeaderToolbarComponent } from './header-toolbar/header-toolbar.component';
import { AdminAuthService } from '../../../modules/admin-dashboard/admin-auth.service';
import { WindowRef } from '../../../core/services/browser/window-ref.service';
import { DocumentRef } from '../../../core/services/browser/document-ref.service';
import { GoogleSearchBarUtils } from '../../utils/google-search-bar.utils';

@Component({
  selector: 'app-header',
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    NavigationComponent,
    // SearchBarComponent,
    HeaderToolbarComponent,
    RouterLink,
    GithubUserBadgeComponent,
    NgbInputDatepicker,
    ɵEmptyOutletComponent
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
  google: any;
  private readonly router = inject(Router);

  @Input() showNavigation = true;
  @Input() showMenuToggle = false;
  @Output() menuToggle = new EventEmitter<void>();

  selectedNav = '/';
  isAdminRoute = false;
  userInfo = this.adminAuthService.userInfo;
  isMobileMenuCollapsed = model<boolean>(true);
  headerOffcanvasRef: NgbOffcanvasRef | null = null;

  constructor(
    private readonly renderer: Renderer2,
    private readonly windowRef: WindowRef,
    private readonly documentRef: DocumentRef
  ) {
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
    console.log(content);
    const doc = this.documentRef.nativeDocument;

    if (!doc) {
      return;
    }

    this.headerOffcanvasRef = this.offcanvasService.open(content, {
      ariaLabelledBy: 'offcanvas-basic-title',
      backdrop: true,
      panelClass: 'my-offcanvas',
      position: 'end',
      backdropClass: 'my-offcanvas-backdrop'
    });

    this.headerOffcanvasRef.shown.subscribe(() => {
      GoogleSearchBarUtils.renderOffcanvasGoogleSearchBar(
        this.renderer,
        this.windowRef,
        this.documentRef
      );

      // 👇 ADD THIS
      GoogleSearchBarUtils.addCustomClassToSearchBar(
        this.renderer,
        doc
      );
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
