import { Component, inject, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageService } from '../../../../core/services/language/language.service';
import { Language } from '../../../../shared/enums/language.enum';
import { ProductDetail } from '../../../../shared/models/product-detail.model';

@Component({
  selector: 'app-product-detail-maven-content',
  imports: [TranslateModule],
  templateUrl: './product-detail-maven-content.component.html',
  styleUrl: './product-detail-maven-content.component.scss'
})
export class ProductDetailMavenContentComponent {
  @Input()
  productDetail!: ProductDetail;
  @Input()
  selectedVersion!: string;

  languageService = inject(LanguageService);

  getProductName() {
    return this.productDetail.names[Language.EN];
  }
}
