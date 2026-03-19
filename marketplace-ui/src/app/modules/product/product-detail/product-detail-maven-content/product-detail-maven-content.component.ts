import { Component, Input } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductModuleContent } from '../../../../shared/models/product-module-content.model';

@Component({
  selector: 'app-product-detail-maven-content',
  imports: [TranslateModule],
  templateUrl: './product-detail-maven-content.component.html',
  styleUrl: './product-detail-maven-content.component.scss'
})
export class ProductDetailMavenContentComponent {
  @Input() productModuleContent!: ProductModuleContent;
  @Input() selectedVersion!: string;
  @Input() productName!: string;
}
