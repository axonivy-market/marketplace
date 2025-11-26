import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { CommonModule } from '@angular/common';
import { ERROR_PAGE_PATH } from './shared/constants/common.constant';
import { AfterViewInit, Component, OnInit, inject, Renderer2 } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  NavigationError,
  Router,
  RouterOutlet,
  Event
} from '@angular/router';
import { BackToTopComponent } from "./shared/components/back-to-top/back-to-top.component";
import { GoogleSearchBarUtils } from './shared/utils/google-search-bar.utils';
import { WindowRef } from './core/services/browser/window-ref.service';
import { DocumentRef } from './core/services/browser/document-ref.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, CommonModule, BackToTopComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, AfterViewInit {
  routingQueryParamService = inject(RoutingQueryParamService);
  route = inject(ActivatedRoute);
  isMobileMenuCollapsed = true;
  isAdminRoute = false;

  constructor(private readonly router: Router, private readonly renderer: Renderer2, private readonly windowRef: WindowRef, private readonly documentRef: DocumentRef) {
    this.updateAdminState(this.router.url);
  }

  ngOnInit(): void {
    this.router.events.subscribe((event: Event) => {
      if (event instanceof NavigationError) {
        this.router.navigate([ERROR_PAGE_PATH]);
      }
      if (event instanceof NavigationEnd) {
        const url = event.urlAfterRedirects ?? event.url;
        this.updateAdminState(url);
      }
    });

    this.routingQueryParamService.getNavigationStartEvent().subscribe(() => {
      if (!this.routingQueryParamService.isDesignerEnv()) {
        this.route.queryParams.subscribe(params => {
          this.routingQueryParamService.checkSessionStorageForDesignerEnv(params);
          this.routingQueryParamService.checkSessionStorageForDesignerVersion(params);
        });
      }
    });
  }

  ngAfterViewInit(): void {
    GoogleSearchBarUtils.renderGoogleSearchBar(this.renderer, this.windowRef, this.documentRef);
  }

  private updateAdminState(url: string): void {
    this.isAdminRoute = url.startsWith('/octopus');
  }
}