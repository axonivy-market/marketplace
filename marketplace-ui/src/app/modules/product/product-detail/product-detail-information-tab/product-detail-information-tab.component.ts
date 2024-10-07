import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetailService } from '../product-detail.service';
import { VERSION } from '../../../../shared/constants/common.constant';

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
  languageService = inject(LanguageService);
  productDetailService = inject(ProductDetailService);

  ngOnChanges(changes: SimpleChanges): void {
    let version = '';
    const changedSelectedVersion = changes['selectedVersion'];
    if (changedSelectedVersion && changedSelectedVersion.currentValue === changedSelectedVersion.previousValue) {
      return;
    }
    const changedProduct = changes['productDetail'];
    if (changedProduct && changedProduct.currentValue !== changedProduct.previousValue) {
      version = this.productDetail.newestReleaseVersion;
    } else {
      version = this.selectedVersion;
    }
    // Invalid version
    if (version == undefined || version === '') {
      return;
    }

    this.productDetailService.getExteralDocumentLinkForProductByVersion(this.productDetail.id, this.extractVersionValue(version))
      .subscribe((repsonse)=> {
        this.externalDocumentLink = repsonse as string;
    });
  }

  getDisplayVersion() {
    return this.extractVersionValue(this.selectedVersion);
  }

  extractVersionValue(versionDisplayName: string) {
    return versionDisplayName.replace(VERSION.displayPrefix, '').replace(VERSION.tagPrefix, '');
  }
}
