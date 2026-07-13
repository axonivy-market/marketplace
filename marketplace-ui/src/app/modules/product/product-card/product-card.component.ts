import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Component, inject, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { Product } from '../../../shared/models/product.model';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductComponent } from '../product.component';
import {
  DEFAULT_IMAGE_URL,
  DARK_INTERNAL_BADGE_URL,
  LIGHT_INTERNAL_BADGE_URL,
} from '../../../shared/constants/common.constant';

@Component({
  selector: 'app-product-card',
  imports: [CommonModule, MultilingualismPipe, TranslateModule, NgOptimizedImage],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent implements OnInit, OnChanges {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  isShowInRESTClientEditor = inject(ProductComponent).isRESTClient();

  private _product!: Product;

  @Input()
  set product(value: Product) {
    this._product = value;
    this.configureLogos();
  }

  get product(): Product {
    return this._product;
  }

  logoUrl = DEFAULT_IMAGE_URL;
  logoDarkUrl = DEFAULT_IMAGE_URL;
  smallBadgeLightUrl = '';
  smallBadgeDarkUrl = '';

  ngOnInit(): void {
    this.configureLogos();
    this.logoUrl = this.product.logoUrl;
    this.logoDarkUrl = this.product.logoDarkUrl?.trim() ? this.product.logoDarkUrl : this.product.logoUrl;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['product'] && !changes['product'].isFirstChange()) {
      this.configureLogos();
    }
  }

  private configureLogos(): void {
    if (!this.product) {
      return;
    }

    if (this.product.internal) {
      this.smallBadgeLightUrl = LIGHT_INTERNAL_BADGE_URL;
      this.smallBadgeDarkUrl = DARK_INTERNAL_BADGE_URL;
    } else {
      const hasVendorLogo = !!this.product.badgeUrl?.trim();
      const hasVendorLogoDark = !!this.product.badgeDarkUrl?.trim();

      if (hasVendorLogo || hasVendorLogoDark) {
        this.smallBadgeLightUrl = this.product.badgeUrl || '';
        this.smallBadgeDarkUrl = this.product.badgeDarkUrl || this.product.badgeUrl || '';
      } else {
        this.smallBadgeLightUrl = '';
        this.smallBadgeDarkUrl = '';
      }
    }
    console.log('ProductCardComponent: smallBadgeLightUrl:', this.smallBadgeLightUrl);
    console.log('ProductCardComponent: smallBadgeDarkUrl:', this.smallBadgeDarkUrl);
  }

  onLogoError() {
    this.logoUrl = DEFAULT_IMAGE_URL;
    this.logoDarkUrl = DEFAULT_IMAGE_URL;
  }
}
