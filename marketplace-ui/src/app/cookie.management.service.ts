import { computed, Injectable, signal } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DESIGNER_COOKIE_VARIABLE } from './shared/constants/common.constant';
import {
  Router,
  Params,
  NavigationStart,
  ActivatedRoute
} from '@angular/router';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class CookieManagementService {
  isDesigner = signal(false);
  isDesignerEnv = computed(() => this.isDesigner());
  designerVersion = signal('');
  resultsOnly = WritableS
  isResultsOnly = computed(() => this.resultsOnly());

  constructor(
    private cookieService: CookieService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.getNavigationStartEvent().subscribe(() => {
      if (!this.isDesigner()) {
        this.isDesigner.set(
          this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) ==
            DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer
        );
      }
    });
  }

  ngOnInit(): void {
    // Accessing query parameters
    this.resultsOnly = this.route.snapshot.queryParamMap.has('resultsOnly');
    console.log('resultsOnly:', this.resultsOnly);
  }

  // ngOnInit(): void {
  //   this.route.queryParams.subscribe(params => {
  //     if (params['resultsOnly']) {
  //       this.resultsOnly.set(true);
  //       console.log('resultsOnly:', this.resultsOnly);
  //     }
  //   });
  // }

  checkCookieForDesignerVersion(params: Params) {
    const versionParam = params[DESIGNER_COOKIE_VARIABLE.ivyVersionParamName];
    if (versionParam != undefined) {
      this.cookieService.set(
        DESIGNER_COOKIE_VARIABLE.ivyVersionParamName,
        versionParam
      );
      this.designerVersion.set(versionParam);
    }
  }

  checkCookieForDesignerEnv(params: Params) {
    const ivyViewerParam = params[DESIGNER_COOKIE_VARIABLE.ivyViewerParamName];
    if (ivyViewerParam == DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer) {
      this.cookieService.set(
        DESIGNER_COOKIE_VARIABLE.ivyViewerParamName,
        ivyViewerParam
      );
      this.isDesigner.set(true);
    }
  }

  getDesignerVersionFromCookie() {
    if (this.designerVersion() == '') {
      this.designerVersion.set(
        this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName)
      );
    }
    return this.designerVersion();
  }

  isDesignerViewer() {
    if (!this.isDesigner()) {
      this.isDesigner.set(
        this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) ==
          DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer
      );
    }
    return this.isDesigner();
  }

  getNavigationStartEvent(): Observable<NavigationStart> {
    return this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    ) as Observable<NavigationStart>;
  }
}
