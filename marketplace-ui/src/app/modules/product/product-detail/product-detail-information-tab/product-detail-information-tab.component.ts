import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { ThemeBasedImagePipe } from '../../../../shared/pipes/theme-based-image.pipe';

@Component({
  selector: 'app-product-detail-information-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule, ThemeBasedImagePipe],
  templateUrl: './product-detail-information-tab.component.html',
  styleUrl: './product-detail-information-tab.component.scss'
})
export class ProductDetailInformationTabComponent {
  @Input()
  productDetail!: ProductDetail;
  @Input()
  selectedVersion!: string;

  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
}
