import { Component, inject, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductModuleContent } from '../../../../shared/models/product-module-content.model';
import { LanguageService } from '../../../../core/services/language/language.service';

@Component({
  selector: 'app-product-detail-maven-content',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './product-detail-maven-content.component.html',
  styleUrl: './product-detail-maven-content.component.scss'
})
export class ProductDetailMavenContentComponent {
  @Input()
  productModuleContent!: ProductModuleContent;
  @Input()
  selectedVersion!: string;

  languageService = inject(LanguageService);
}
