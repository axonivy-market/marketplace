
import { Injectable, Signal, signal } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DESIGNER_COOKIE_VARIABLE } from './shared/constants/common.constant';
import {
  Router,
  NavigationEnd,
  Params
} from '@angular/router';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class CookieManagementService {
  private isDesigner = signal(false);
  constructor(private cookieService: CookieService, private router: Router) { }

  checkCookieForDesignerVersion(params: Params) {
    const versionParam = params[DESIGNER_COOKIE_VARIABLE.ivyVersionParamName];
    if (versionParam != undefined) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName, versionParam);
    }
  }

  checkCookieForDesignerEnv(params: Params) {
    const ivyViewerParam = params[DESIGNER_COOKIE_VARIABLE.ivyViewerParamName];
    if (ivyViewerParam == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName, ivyViewerParam);
      this.isDesigner.set(true);
    }
  }

  isDesignerEnv() {
    if(!this.isDesigner()) {
      this.isDesigner.set(this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer);
    }
    return this.isDesigner();
  }

  getNavigationEndEvents(): Observable<NavigationEnd> {
    return this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ) as Observable<NavigationEnd>;
  }
}