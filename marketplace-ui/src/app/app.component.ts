import { Component, inject, signal } from '@angular/core';
import {
  RouterOutlet,
  ActivatedRoute,
  Router,
  NavigationEnd,
  Params
} from '@angular/router';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { filter } from 'rxjs';
import { DESIGNER_COOKIE_VARIABLE } from './shared/constants/common.constant';
import { CookieService } from 'ngx-cookie-service';
import { set } from 'yaml/dist/schema/yaml-1.1/set';

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
    private readonly cookieService: CookieService
  ) { }

  private checkCookieForDesignerVersion(params: Params) {
    const versionParam = params[DESIGNER_COOKIE_VARIABLE.ivyVersionParamName];
    if (versionParam != undefined) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName, versionParam);
    }
  }

  private checkCookieForDesignerEnv(params: Params) {
    const ivyViewerParam = params[DESIGNER_COOKIE_VARIABLE.ivyViewerParamName];
    if (ivyViewerParam == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName, ivyViewerParam);
      this.isDesignerViewer.set(true);
    }
  }

  private isBrowserHaveDesignerEnvCookie() {
    return this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer
  }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        if (this.isBrowserHaveDesignerEnvCookie()) {
          this.isDesignerViewer.set(true);
        } else {
          this.route.queryParams.subscribe(params => {
            this.checkCookieForDesignerEnv(params);
            this.checkCookieForDesignerVersion(params);
          })
        }
      })
  }
}
