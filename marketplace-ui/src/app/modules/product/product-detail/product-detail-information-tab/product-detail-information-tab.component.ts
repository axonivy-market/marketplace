import { CommonModule } from '@angular/common';
import {
  Component,
  inject,
  Input,
  OnChanges,
  SimpleChange,
  SimpleChanges
} from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { ProductDetail } from '../../../../shared/models/product-detail.model';
import { LanguageService } from '../../../../core/services/language/language.service';
import { ProductDetailService } from '../product-detail.service';
import { VERSION } from '../../../../shared/constants/common.constant';
import { LoadingService } from '../../../../core/services/loading/loading.service';
import { ThemeService } from '../../../../core/services/theme/theme.service';
import { IsEmptyObjectPipe } from '../../../../shared/pipes/is-empty-object.pipe';
import { LoadingComponentId } from '../../../../shared/enums/loading-component-id';
import { Router } from '@angular/router';
const SELECTED_VERSION = 'selectedVersion';
const PRODUCT_DETAIL = 'productDetail';
const SHIELDS_BASE_URL = 'https://img.shields.io/github/actions/workflow/status';
const SHIELDS_WORKFLOW = 'ci.yml';
const SHIELDS_BRANCH = 'master';

@Component({
  selector: 'app-product-detail-information-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule, IsEmptyObjectPipe],
  templateUrl: './product-detail-information-tab.component.html',
  styleUrl: './product-detail-information-tab.component.scss'
})

export class ProductDetailInformationTabComponent implements OnChanges {
  @Input()
  productDetail!: ProductDetail;
  @Input()
  selectedVersion!: string;
  protected LoadingComponentId = LoadingComponentId;
  externalDocumentLink = '';
  displayVersion = '';
  displayExternalDocName: string | null = '';
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  productDetailService = inject(ProductDetailService);
  loadingService = inject(LoadingService);
  router = inject(Router);
  shieldsBadgeUrl: string = '';

  ngOnInit(): void {
    this.displayVersion = this.extractVersionValue(this.selectedVersion);
    this.shieldsBadgeUrl = this.getShieldsBadgeUrl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    let version = '';
    if (this.isVersionUnchangedOrFirstChange(changes[SELECTED_VERSION])) {
      return;
    }
    const changedProduct = changes[PRODUCT_DETAIL];
    if (this.isProductChanged(changedProduct)) {
      version = this.productDetail.newestReleaseVersion;
    } else {
      version = this.selectedVersion;
    }
    // Invalid version
    if (version === undefined || version === '') {
      return;
    }

    this.productDetailService
      .getExternalDocumentForProductByVersion(
        this.productDetail.id,
        this.extractVersionValue(version)
      )
      .subscribe({
        next: response => {
          if (response) {
            this.externalDocumentLink = response.relativeLink;
            this.displayExternalDocName = response.artifactName;
          } else {
            this.resetValues();
          }
        },
        error: () => {
          this.resetValues();
        }
      });
    this.displayVersion = this.extractVersionValue(this.selectedVersion);
    this.shieldsBadgeUrl = this.getShieldsBadgeUrl();
  }
  getShieldsBadgeUrl(): string {
    if (!this.productDetail || !this.productDetail.sourceUrl) {
      return '';
    }
    try {
      return `${SHIELDS_BASE_URL}/axonivy-market/${this.productDetail.id}/${SHIELDS_WORKFLOW}?branch=${SHIELDS_BRANCH}`;
    } catch {
      return '';
    }
  }

  isVersionUnchangedOrFirstChange(change: SimpleChange | undefined): boolean {
    return (
      !!change &&
      (change.currentValue === change.previousValue || change.firstChange)
    );
  }

  resetValues() {
    this.externalDocumentLink = '';
    this.displayExternalDocName = '';
  }

  extractVersionValue(versionDisplayName: string) {
    return versionDisplayName.replace(VERSION.displayPrefix, '');
  }

  //  To ensure the function always returns a boolean, you can explicitly coerce the result into a boolean using the !! operator or default it to false
  //  Adding !! in case of changedProduct is undefined, it will return false instead of returning undefined
  isProductChanged(changedProduct: SimpleChange) {
    return !!(changedProduct?.previousValue &&
      Object.keys(changedProduct.previousValue).length > 0 &&
      changedProduct.currentValue !== changedProduct.previousValue
    );
  }
  onBadgeClick() {
     this.router.navigate(['/monitoring']);
  }
}
