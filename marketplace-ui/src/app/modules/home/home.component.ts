import { Component, inject } from '@angular/core';
import { ProductComponent } from '../product/product.component';
import { TranslateService } from '@ngx-translate/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [ProductComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  translateService = inject(TranslateService);

  constructor(private readonly titleService: Title) {
    this.titleService.setTitle(this.translateService.instant('common.branch'));
  }
}
