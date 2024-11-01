import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetailService } from '../product-detail.service';
import { VERSION } from '../../../../shared/constants/common.constant';
import { LoadingService } from '../../../../core/services/loading/loading.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';

const SELECTED_VERSION = 'selectedVersion';
const PRODUCT_DETAIL = 'productDetail';
@Component({
  selector: 'app-product-detail-information-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './product-detail-information-tab.component.html',
  styleUrl: './product-detail-information-tab.component.scss'
})
export class ProductDetailInformationTabComponent implements OnChanges {
  @Input()
  productDetail!: ProductDetail;
  @Input()
  selectedVersion!: string;
  externalDocumentLink = '';
  displayVersion = '';
  displayExternalDocName: string | null = '';
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  productDetailService = inject(ProductDetailService);
  loadingService = inject(LoadingService);

  ngOnChanges(changes: SimpleChanges): void {
    let version = '';
    const changedSelectedVersion = changes[SELECTED_VERSION];
    if (changedSelectedVersion && changedSelectedVersion.currentValue === changedSelectedVersion.previousValue) {
      return;
    }
    const changedProduct = changes[PRODUCT_DETAIL];
    if (changedProduct && changedProduct.currentValue !== changedProduct.previousValue) {
      version = this.productDetail.newestReleaseVersion;
    } else {
      version = this.selectedVersion;
    }
    // Invalid version
    if (version === undefined || version === '') {
      return;
    }

    this.productDetailService.getExteralDocumentForProductByVersion(this.productDetail.id, this.extractVersionValue(version))
      .subscribe({
        next: response => {
          this.externalDocumentLink = response.relativeLink;
          this.displayExternalDocName = response.artifactName;
          this.loadingService.hide();
        },
        error: () => {
          this.externalDocumentLink = '';
          this.displayExternalDocName = '';
          this.loadingService.hide();
        }
      });
    this.displayVersion = this.extractVersionValue(this.selectedVersion);
  }

  extractVersionValue(versionDisplayName: string) {
    return versionDisplayName.replace(VERSION.displayPrefix, '');
  }
}
