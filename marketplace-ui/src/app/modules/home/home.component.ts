import { Component, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { ProductComponent } from '../product/product.component';
import { Router } from '@angular/router';
import { PageTitleService } from '../../shared/services/page-title.service';

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

  ngOnInit(): void {
    this.pageTitleService.setTitleOnLangChange('common.branch');
  }
}
