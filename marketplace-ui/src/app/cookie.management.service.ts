
import { computed, Injectable, Signal, signal } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DESIGNER_COOKIE_VARIABLE } from './shared/constants/common.constant';
import {
  Router,
  NavigationEnd,
  Params,
  NavigationStart
} from '@angular/router';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class CookieManagementService {
  private isDesigner = signal(false);
  isDesignerEnv = computed(() => this.isDesigner());
  designerVersion = signal('');

  constructor(private cookieService: CookieService, private router: Router) { 
    this.getNavigationStartEvent().subscribe(()=> {
      if (!this.isDesigner()) {
        this.isDesigner.set(this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer);
      }
    })
  }

  checkCookieForDesignerVersion(params: Params) {
    const versionParam = params[DESIGNER_COOKIE_VARIABLE.ivyVersionParamName];
    if (versionParam != undefined) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName, versionParam);
      this.designerVersion.set(versionParam);
    }
  }

  checkCookieForDesignerEnv(params: Params) {
    const ivyViewerParam = params[DESIGNER_COOKIE_VARIABLE.ivyViewerParamName];
    if (ivyViewerParam == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer) {
      this.cookieService.set(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName, ivyViewerParam);
      this.isDesigner.set(true);
    }
  }

  getDesignerVersionFromCookie() {
    if (this.designerVersion() == '') {
      this.designerVersion.set(this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName))
    }
    return this.designerVersion();
  }

  isDesignerViewer() {
    if (!this.isDesigner()) {
      this.isDesigner.set(this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer);
    }
    return this.isDesigner();
  }

  getNavigationStartEvent():Observable<NavigationStart>{
    return this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    ) as Observable<NavigationStart>;
  }
}