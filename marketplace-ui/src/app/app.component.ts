import { Component, inject, signal } from '@angular/core';
import {
  RouterOutlet,
  ActivatedRoute,
  Router,
  NavigationEnd,
} from '@angular/router';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { CookieManagementService } from './cookie.management.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  loadingService = inject(LoadingService);
  isDesignerViewer = signal(false);

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private readonly cookieManagementService: CookieManagementService
  ) { }

  ngOnInit(): void {
    this.cookieManagementService.getNavigationEndEvents().subscribe(() => {
      if (this.cookieManagementService.isBrowserHaveDesignerEnvCookie()) {
        this.isDesignerViewer.set(true);
      } else {
        this.route.queryParams.subscribe(params => {
          this.cookieManagementService.checkCookieForDesignerEnv(params, this.isDesignerViewer);
          this.cookieManagementService.checkCookieForDesignerVersion(params);
        })
      }
    })
  }
}
