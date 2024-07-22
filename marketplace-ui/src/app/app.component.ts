import { Component, inject, signal } from '@angular/core';
import {
  RouterOutlet,
  ActivatedRoute,
  Router,
  NavigationEnd
} from '@angular/router';
import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { filter } from 'rxjs';
import { DESIGNER_VARIABLE } from './shared/constants/common.constant';

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
  ivyVersion = signal('');

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.route.queryParams.subscribe(params => {
          if (params['ivy-viewer'] == DESIGNER_VARIABLE.viewer) {
            this.isDesignerViewer.set(true);
          }
          if (params["ivy-version"]) {
            this.ivyVersion.set(params["ivy-version"]);
          }
        });
      });
  }
}
