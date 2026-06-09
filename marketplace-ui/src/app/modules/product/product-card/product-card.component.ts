import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Product } from '../../../shared/models/product.model';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductComponent } from '../product.component';
import { DEFAULT_IMAGE_URL } from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-product-card',
  imports: [CommonModule, MultilingualismPipe, TranslateModule, NgOptimizedImage],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isShowInRESTClientEditor = inject(ProductComponent).isRESTClient();

  @Input() product!: Product;

  logoUrl = DEFAULT_IMAGE_URL;
  logoDarkUrl = DEFAULT_IMAGE_URL;

  ngOnInit(): void {
    this.logoUrl = this.product.logoUrl;
    this.logoDarkUrl = !this.product.logoDarkUrl?.trim() ? this.product.logoUrl : this.product.logoDarkUrl;
  }

  onLogoError() {
    if (this.themeService.isDarkMode()) {
      this.logoDarkUrl = DEFAULT_IMAGE_URL;
    } else {
      this.logoUrl = DEFAULT_IMAGE_URL;
    }
  }
}
