import { Component, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { ProductComponent } from '../product/product.component';
import { Router } from '@angular/router';
import { PageTitleService } from '../../shared/services/page-title.service';
import { FaviconService } from '../../shared/services/favicon.service';
import {
  FAVICON_DEFAULT_URL,
  FAVICON_DEFAULT_TYPE
} from '../../shared/constants/common.constant';

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
  pageTitleService = inject(PageTitleService);
  faviconService = inject(FaviconService);

  ngOnInit(): void {
    this.faviconService.setFavicon(FAVICON_DEFAULT_URL, FAVICON_DEFAULT_TYPE);
    this.pageTitleService.setTitleOnLangChange('common.branch');
  }
}
