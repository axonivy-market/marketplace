import { Component, inject } from '@angular/core';
import { RouterOutlet, ActivatedRoute } from '@angular/router';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  loadingService = inject(LoadingService);
  routingQueryParamService = inject(RoutingQueryParamService);
  route = inject(ActivatedRoute);

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
}
