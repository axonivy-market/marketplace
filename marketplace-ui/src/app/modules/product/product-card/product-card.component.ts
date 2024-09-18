import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Product } from '../../../shared/models/product.model';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductComponent } from '../product.component';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, MultilingualismPipe, TranslateModule, NgOptimizedImage],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isShowInRESTClientEditor = inject(ProductComponent).isRESTClient();

  @Input() product!: Product;
  logoUrl = '/assets/images/misc/axonivy-logo-round.png';

  ngOnInit(): void {
    this.logoUrl = this.product.logoUrl;
  }

  onErrorLogo() {
    this.logoUrl = '/assets/images/misc/axonivy-logo-round.png';
  }
}
