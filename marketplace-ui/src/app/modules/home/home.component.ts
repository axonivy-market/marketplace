import { Component, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { ProductComponent } from '../product/product.component';
import { Router } from '@angular/router';
import { API_URI } from '../../shared/constants/api.constant';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [ProductComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  router = inject(Router);
  titleService = inject(Title);
  translateService = inject(TranslateService);
  constructor() {}

  ngOnInit(): void {
    // Set the title initially
    this.updateHomePageTitle();

    // Update the title whenever the language changes
    this.translateService.onLangChange.subscribe(() => {
      if (this.router.url === API_URI.APP) {
        this.updateHomePageTitle();
      }
    });
  }

  private updateHomePageTitle(): void {
    // Get the translated title key and set it as the title
    this.translateService
      .get('common.branch')
      .subscribe((translatedTitle: string) => {
        this.titleService.setTitle(translatedTitle);
      });
  }
}
