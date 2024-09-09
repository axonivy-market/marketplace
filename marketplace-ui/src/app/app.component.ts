import { Component, inject, Input } from '@angular/core';
import { RouterOutlet, ActivatedRoute } from '@angular/router';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  loadingService = inject(LoadingService);
  routingQueryParamService = inject(RoutingQueryParamService);
  route = inject(ActivatedRoute);

  @Input() headerClass = '';

  constructor() {}

  ngOnInit(): void {
    this.routingQueryParamService.getNavigationStartEvent().subscribe(() => {
      if (!this.routingQueryParamService.isDesignerEnv()) {
        this.route.queryParams.subscribe(params => {
          this.routingQueryParamService.checkCookieForDesignerEnv(params);
          this.routingQueryParamService.checkCookieForDesignerVersion(params);
        });
      }
    });
  }

  toggleMobileHeader(isMobileMenuCollapsed: boolean) {
    this.headerClass = isMobileMenuCollapsed ? '' : 'header-mobile';
  }
}
