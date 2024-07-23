import { Component, inject } from '@angular/core';
import {
  RouterOutlet,
  ActivatedRoute
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
  cookieManagementService = inject( CookieManagementService);

  constructor(
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.cookieManagementService.getNavigationEndEvents().subscribe(() => {
      if (!this.cookieManagementService.isDesignerEnv()) {
        this.route.queryParams.subscribe(params => {
          this.cookieManagementService.checkCookieForDesignerEnv(params);
          this.cookieManagementService.checkCookieForDesignerVersion(params);
        })
      }
    })
  }
}
