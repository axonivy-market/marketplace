import { CommonModule, isPlatformBrowser, NgOptimizedImage } from '@angular/common';
import { Component, Inject, inject, Input, PLATFORM_ID } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Product } from '../../../shared/models/product.model';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductComponent } from '../product.component';
import { DEFAULT_IMAGE_URL } from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, MultilingualismPipe, TranslateModule, NgOptimizedImage],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent {
  isBrowser: boolean;
  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isShowInRESTClientEditor = inject(ProductComponent).isRESTClient();

  @Input() product!: Product;
  logoUrl = DEFAULT_IMAGE_URL;

  ngOnInit(): void {
    this.logoUrl = this.product.logoUrl;
  }

  onLogoError() {
    this.logoUrl = DEFAULT_IMAGE_URL;
  }
}
